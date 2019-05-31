package io.github.phantamanta44.libnine.client.model;

import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.github.phantamanta44.libnine.util.helper.JsonUtils9;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParameterizedItemModel implements IModel {

    private final TObjectIntMap<String> indexMap;
    private final Map<Mutation, IModel> children;

    ParameterizedItemModel(ParameterizedItemModelLoader.ResourceInjector resourceInjector,
                           JsonObject archetype, @Nullable Table<String, String, JsonElement> mutations) {
        this.indexMap = new TObjectIntHashMap<>();
        if (mutations != null) {
            String[] keys = new String[mutations.rowKeySet().size()];
            int index = 0;
            for (String key : mutations.rowKeySet()) {
                this.indexMap.put(key, index);
                keys[index] = key;
                index++;
            }
            this.children = calculateCartesianProduct(keys, mutations, 0)
                    .collect(Collectors.toMap(m -> m, m -> mutateModel(archetype, mutations, m).compile(resourceInjector)));
        } else {
            this.children = Collections.singletonMap(new Mutation(this), new IChildModel.JsonModel(archetype).compile(resourceInjector));
        }
    }

    private Stream<Mutation> calculateCartesianProduct(String[] keys, Table<String, String, JsonElement> mutations, int index) {
        Stream<Mutation> prev = index == keys.length - 1
                ? Stream.of(new Mutation(this)) : calculateCartesianProduct(keys, mutations, index + 1);
        return prev.flatMap(m -> mutations.row(keys[index]).keySet().stream().map(v -> m.mutateCloning(keys[index], v)));
    }

    private IChildModel mutateModel(JsonObject model, Table<String, String, JsonElement> mutations, Mutation mutation) {
        JsonObject result = JsonUtils9.copy(model);
        for (String key : indexMap.keySet()) {
            JsonElement mutator = mutations.get(key, mutation.values[indexMap.get(key)]);
            if (mutator.isJsonObject()) {
                JsonUtils9.merge(result, mutator.getAsJsonObject());
            } else if (mutator.isJsonPrimitive()) {
                JsonPrimitive mutatorPrim = mutator.getAsJsonPrimitive();
                if (mutatorPrim.isString()) {
                    String mutatorStr = mutatorPrim.getAsString();
                    int spaceIndex = mutatorStr.indexOf(' ');
                    if (spaceIndex != -1) {
                        String arg = mutatorStr.substring(spaceIndex + 1);
                        if (mutatorStr.substring(0, spaceIndex).equals("supplant")) {
                            return new IChildModel.Supplant(new ResourceLocation(arg));
                        }
                        throw new IllegalArgumentException("Unknown parametric mutator directive: " + mutatorStr);
                    } else {
                        throw new IllegalArgumentException("Unknown zero-arg mutator directive: " + mutatorStr);
                    }
                } else {
                    throw new IllegalArgumentException("Cannot parse mutator: " + mutator);
                }
            } else {
                throw new IllegalArgumentException("Cannot parse mutator: " + mutator);
            }
        }
        return new IChildModel.JsonModel(result);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureBaker) {
        return new ParameterizedItemModelBakedModelDelegator(this, children.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().bake(state, format, textureBaker))));
    }

    private interface IChildModel {

        IModel compile(ParameterizedItemModelLoader.ResourceInjector resourceInjector);

        class JsonModel implements IChildModel {

            private final JsonObject src;

            JsonModel(JsonObject src) {
                this.src = src;
            }

            @Override
            public IModel compile(ParameterizedItemModelLoader.ResourceInjector resourceInjector) {
                ResourceLocation injected = resourceInjector.injectResource(JsonUtils9.GSON.toJson(src));
                try {
                    return ModelLoaderRegistry.getModel(injected);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

        class Supplant implements IChildModel {

            private final ResourceLocation suppletion;

            Supplant(ResourceLocation suppletion) {
                this.suppletion = suppletion;
            }

            @Override
            public IModel compile(ParameterizedItemModelLoader.ResourceInjector resourceInjector) {
                try {
                    return ModelLoaderRegistry.getModel(suppletion);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    private static class ParameterizedItemModelBakedModelDelegator implements IBakedModel {

        private final ParameterizedItemModel model;
        private final Map<Mutation, IBakedModel> children;
        private final IBakedModel defaultDelegate;

        ParameterizedItemModelBakedModelDelegator(ParameterizedItemModel model, Map<Mutation, IBakedModel> children) {
            this.model = model;
            this.children = children;
            this.defaultDelegate = children.values().iterator().next();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return defaultDelegate.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return defaultDelegate.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return defaultDelegate.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return defaultDelegate.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return defaultDelegate.getParticleTexture();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return new ParameterizedItemModelOverrideList(this);
        }

        private static class ParameterizedItemModelOverrideList extends ItemOverrideList {

            private final ParameterizedItemModelBakedModelDelegator container;

            public ParameterizedItemModelOverrideList(ParameterizedItemModelBakedModelDelegator container) {
                super(Collections.emptyList());
                this.container = container;
            }

            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                Mutation mutation = new Mutation(container.model);
                ((IContextSensitive)stack.getItem()).getModelMutations(stack, world, entity, mutation);
                if (!container.children.containsKey(mutation)) {
                    throw new NoSuchElementException("Invalid PI mutation: " + mutation);
                }
                IBakedModel delegate = container.children.get(mutation);
                return delegate.getOverrides().handleItemState(delegate, stack, world, entity);
            }

        }

    }

    public interface IParamaterized extends IContextSensitive {

        void getModelMutations(ItemStack stack, Mutation m);

        @Override
        default void getModelMutations(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase holder, Mutation m) {
            getModelMutations(stack, m);
        }

    }

    public interface IContextSensitive {

        void getModelMutations(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase holder, Mutation m);

    }

    public static class Mutation {

        private final TObjectIntMap<String> indexMap;
        private final String[] values;

        public Mutation(ParameterizedItemModel model) {
            this.indexMap = model.indexMap;
            this.values = new String[model.indexMap.size()];
        }

        private Mutation(Mutation original) {
            this.indexMap = original.indexMap;
            this.values = Arrays.copyOf(original.values, original.values.length);
        }

        public Mutation mutate(String key, String value) {
            values[indexMap.get(key)] = value;
            return this;
        }

        Mutation mutateCloning(String key, String value) {
            return values[indexMap.get(key)] == null ? mutate(key, value) : new Mutation(this).mutate(key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Mutation)) return false;
            for (int i = 0; i < values.length; i++) {
                if (!((Mutation)o).values[i].equals(values[i])) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < values.length; i++) hash |= (values[i].hashCode() << (i * 4));
            return hash;
        }

        @Override
        public String toString() {
            return "Mutation { " + indexMap.keySet().stream()
                    .map(k -> String.format("%s: %s", k, values[indexMap.get(k)]))
                    .collect(Collectors.joining(", ")) + " }";
        }

    }

}
