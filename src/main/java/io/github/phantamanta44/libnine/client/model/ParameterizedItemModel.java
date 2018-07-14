package io.github.phantamanta44.libnine.client.model;

import com.google.common.collect.Table;
import com.google.gson.JsonObject;
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
                           JsonObject archetype, @Nullable Table<String, String, JsonObject> mutations) {
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
                    .collect(Collectors.toMap(m -> m, m -> compileChildModel(resourceInjector, mutateModel(archetype, mutations, m))));
        } else {
            this.children = Collections.singletonMap(new Mutation(this), compileChildModel(resourceInjector, archetype));
        }
    }

    private Stream<Mutation> calculateCartesianProduct(String[] keys, Table<String, String, JsonObject> mutations, int index) {
        Stream<Mutation> prev = index == keys.length - 1
                ? Stream.of(new Mutation(this)) : calculateCartesianProduct(keys, mutations, index + 1);
        return prev.flatMap(m -> mutations.row(keys[index]).keySet().stream().map(v -> m.mutateCloning(keys[index], v)));
    }

    private JsonObject mutateModel(JsonObject model, Table<String, String, JsonObject> mutations, Mutation mutation) {
        JsonObject result = JsonUtils9.copy(model);
        for (String key : indexMap.keySet()) {
            JsonUtils9.merge(result, mutations.get(key, mutation.values[indexMap.get(key)]));
        }
        return result;
    }

    private IModel compileChildModel(ParameterizedItemModelLoader.ResourceInjector resourceInjector, JsonObject src) {
        ResourceLocation injected = resourceInjector.injectResource(JsonUtils9.GSON.toJson(src));
        try {
            return ModelLoaderRegistry.getModel(injected);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureBaker) {
        return new ParameterizedItemModelBakedModelDelegator(this, children.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().bake(state, format, textureBaker))));
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
                ((IParamaterized)stack.getItem()).getModelMutations(stack, mutation);
                if (!container.children.containsKey(mutation)) {
                    throw new NoSuchElementException("Invalid PI mutation: " + mutation);
                }
                return container.children.get(mutation);
            }

        }

    }

    public interface IParamaterized {

        void getModelMutations(ItemStack stack, Mutation m);

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
