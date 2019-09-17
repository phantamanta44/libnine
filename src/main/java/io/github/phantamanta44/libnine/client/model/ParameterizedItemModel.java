package io.github.phantamanta44.libnine.client.model;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.phantamanta44.libnine.util.ImpossibilityRealizedException;
import io.github.phantamanta44.libnine.util.helper.JsonUtils9;
import io.github.phantamanta44.libnine.util.tuple.IPair;
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

public class ParameterizedItemModel implements IModel {

    private final Map<String, IPair<Integer, String[]>> mutKeyMap;
    private final List<IPair<String, String[]>> mutKeyMapInv;
    private final MutationState[] mutationSet;
    private final IModel[] models;

    ParameterizedItemModel(ParameterizedItemModelLoader.ResourceInjector resourceInjector,
                           JsonObject archetype, @Nullable Table<String, String, JsonElement> mutations) {
        this.mutKeyMap = new HashMap<>();
        this.mutKeyMapInv = new ArrayList<>();
        if (mutations != null) {
            int[] maxValueIndices = new int[mutations.rowKeySet().size()];
            int totalMutCount = 1;
            int index = 0;
            for (Map.Entry<String, Map<String, JsonElement>> keyEntry : mutations.rowMap().entrySet()) {
                Map<String, JsonElement> valueMap = keyEntry.getValue();
                String key = keyEntry.getKey();
                String[] valueSet = valueMap.keySet().toArray(new String[0]);
                mutKeyMap.put(key, IPair.of(index, valueSet));
                mutKeyMapInv.add(IPair.of(key, valueSet));
                int valueCount = valueMap.size();
                maxValueIndices[index] = valueCount - 1;
                totalMutCount *= valueCount;
                ++index;
            }
            this.mutationSet = new MutationState[totalMutCount];
            this.models = new IModel[totalMutCount];
            int[] valueIndices = new int[maxValueIndices.length];
            index = 0;
            outer:
            while (true) {
                int[] currentValueIndices = Arrays.copyOf(valueIndices, valueIndices.length);
                MutationState mut = new MutationState(this, index, currentValueIndices);
                mutationSet[index] = mut;
                for (int i = 0; i < valueIndices.length; i++) {
                    if (++valueIndices[i] > maxValueIndices[i]) {
                        if (i == valueIndices.length - 1) {
                            break outer;
                        }
                        valueIndices[i] = 0;
                    } else {
                        break;
                    }
                }
                ++index;
            }
            for (MutationState mut : mutationSet) {
                mut.cacheAdjacents();
                models[mut.index] = mutateModel(archetype, mutations, mut).compile(resourceInjector);
            }
        } else {
            this.mutationSet = new MutationState[] { new MutationState(this, 0, new int[0]) };
            this.models = new IModel[] { new IChildModel.JsonModel(archetype).compile(resourceInjector) };
        }
    }

    private IChildModel mutateModel(JsonObject model, Table<String, String, JsonElement> mutations, MutationState mutation) {
        JsonObject result = JsonUtils9.copy(model);
        for (int i = 0; i < mutKeyMapInv.size(); i++) {
            IPair<String, String[]> keyEntry = mutKeyMapInv.get(i);
            JsonElement mutator = mutations.get(keyEntry.getA(), keyEntry.getB()[mutation.valueIndices[i]]);
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
        return new ParameterizedItemModelBakedModelDelegator(this,
                Arrays.stream(models).map(m -> m.bake(state, format, textureBaker)).toArray(IBakedModel[]::new));
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
        private final IBakedModel[] children;
        private final IBakedModel defaultDelegate;

        ParameterizedItemModelBakedModelDelegator(ParameterizedItemModel model, IBakedModel[] children) {
            this.model = model;
            this.children = children;
            this.defaultDelegate = children[0];
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

            ParameterizedItemModelOverrideList(ParameterizedItemModelBakedModelDelegator container) {
                super(Collections.emptyList());
                this.container = container;
            }

            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                Mutation mutation = new Mutation(container.model.mutationSet[0]);
                ((IContextSensitive)stack.getItem()).getModelMutations(stack, world, entity, mutation);
                IBakedModel delegate = container.children[mutation.state.index];
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

        private MutationState state;

        Mutation(MutationState initialState) {
            this.state = initialState;
        }

        public Mutation mutate(String key, String value) {
            state = state.mutate(key, value);
            return this;
        }

    }

    private static class MutationState {

        private final ParameterizedItemModel model;
        private final int index;
        private final int[] valueIndices;
        private final Table<String, String, MutationState> adjTable;

        MutationState(ParameterizedItemModel model, int index, int[] valueIndices) {
            this.model = model;
            this.index = index;
            this.valueIndices = valueIndices;
            //noinspection UnstableApiUsage
            this.adjTable = Tables.newCustomTable(new HashMap<>(), HashMap::new);
        }

        void cacheAdjacents() {
            model.mutKeyMap.forEach((key, keyEntry) -> {
                int keyIndex = keyEntry.getA();
                String[] valueSet = keyEntry.getB();
                for (int i = 0; i < valueSet.length; i++) {
                    int valueIndex = i; // no nonfinals in lambdas
                    // TODO this should optimally compute the index directly
                    adjTable.put(key, valueSet[valueIndex], Arrays.stream(model.mutationSet)
                            .filter(mut -> arrayEqualsWithChange(valueIndices, keyIndex, valueIndex, mut.valueIndices))
                            .findAny().orElseThrow(ImpossibilityRealizedException::new));
                }
            });
        }

        public MutationState mutate(String key, String value) {
            MutationState result = adjTable.get(key, value);
            if (result == null) {
                throw new IllegalArgumentException("Invalid mutation: " + key + "=" + value);
            }
            return result;
        }

        @Override
        public String toString() {
            return "Mutation { " + model.mutKeyMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s",
                            entry.getKey(), entry.getValue().getB()[valueIndices[entry.getValue().getA()]]))
                    .collect(Collectors.joining(", ")) + " }";
        }

    }

    private static boolean arrayEqualsWithChange(int[] arr, int changeIndex, int changeValue, int[] toTest) {
        for (int i = 0; i < arr.length; i++) {
            if (toTest[i] != (i == changeIndex ? changeValue : arr[i])) {
                return false;
            }
        }
        return true;
    }

}
