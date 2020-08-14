package io.github.phantamanta44.libnine.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.render.model.BakedQuadList;
import io.github.phantamanta44.libnine.util.world.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class SumItemModel implements IModel {

    private final List<ModelPart> parts;

    public SumItemModel(List<ModelPart> parts) {
        this.parts = parts;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return parts.stream().map(p -> p.path).collect(Collectors.toList());
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return parts.stream().flatMap(p -> p.model.getTextures().stream()).collect(Collectors.toList());
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedSumModel(parts.stream().map(p -> p.model.bake(state, format, bakedTextureGetter)).collect(Collectors.toList()));
    }

    private IModel map(UnaryOperator<IModel> mapper) {
        return new SumItemModel(parts.stream().map(p -> new ModelPart(mapper.apply(p.model), p.path)).collect(Collectors.toList()));
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        return map(m -> m.process(customData));
    }

    @Override
    public IModel smoothLighting(boolean value) {
        return map(m -> m.smoothLighting(value));
    }

    @Override
    public IModel gui3d(boolean value) {
        return map(m -> m.gui3d(value));
    }

    @Override
    public IModel uvlock(boolean value) {
        return map(m -> m.uvlock(value));
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        return map(m -> m.retexture(textures));
    }

    public static class ModelPart {

        final IModel model;
        final ResourceLocation path;

        public ModelPart(IModel model, ResourceLocation path) {
            this.model = model;
            this.path = path;
        }

    }

    public static class Loader implements ICustomModelLoader {

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            // NO-OP
        }

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return L9Models.isOfType(L9Models.getRealModelLocation(modelLocation), "sum");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            JsonObject dto = ResourceUtils.getAsJson(L9Models.getRealModelLocation(modelLocation)).getAsJsonObject();
            List<ModelPart> parts = new ArrayList<>();
            for (JsonElement partDto : dto.getAsJsonArray("parts")) {
                ResourceLocation partPath = new ResourceLocation(partDto.getAsString());
                parts.add(new ModelPart(ModelLoaderRegistry.getModel(partPath), partPath));
            }
            return new SumItemModel(parts);
        }

    }

    private static class BakedSumModel implements IBakedModel {

        private final List<IBakedModel> parts;
        private final boolean ambientOcclusion, gui3d;
        private final TextureAtlasSprite particleTexture;
        private final SumOverrideCache overrideCache;
        private final BakedQuadList quads = new BakedQuadList();

        private BakedSumModel(List<IBakedModel> parts, SumOverrideCache overrideCache) {
            this.parts = parts;
            this.ambientOcclusion = parts.stream().anyMatch(IBakedModel::isAmbientOcclusion);
            this.gui3d = parts.stream().anyMatch(IBakedModel::isGui3d);
            this.particleTexture = parts.stream().map(IBakedModel::getParticleTexture).findFirst()
                    .orElseGet(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
            this.overrideCache = overrideCache;
            for (IBakedModel part : parts) {
                for (EnumFacing face : WorldUtils.FACES_AND_NULL) {
                    for (BakedQuad quad : part.getQuads(null, face, 0L)) {
                        quads.addQuad(face, quad);
                    }
                }
            }
        }

        BakedSumModel(List<IBakedModel> parts) {
            this(parts, new SumOverrideCache());
        }

        @Override
        public ItemOverrideList getOverrides() {
            return overrideCache;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return quads.getQuads(side);
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particleTexture;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return ambientOcclusion;
        }

        @Override
        public boolean isGui3d() {
            return gui3d;
        }

        @SuppressWarnings("deprecation")
        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return parts.get(0).getItemCameraTransforms();
        }

        private static class SumOverrideCache extends ItemOverrideList {

            private final Cache<NBTTagCompound, BakedSumModel> overrideCache = CacheBuilder.newBuilder()
                    .maximumSize(1000) // cache params borrowed from Tinkers' Construct model system, which is under MIT
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

            public SumOverrideCache() {
                super(Collections.emptyList());
            }

            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                if (!(originalModel instanceof BakedSumModel)) {
                    return originalModel;
                }
                NBTTagCompound tag = stack.getTagCompound();
                if (tag == null) {
                    return originalModel;
                }
                try {
                    return overrideCache.get(tag, () -> new BakedSumModel(((BakedSumModel)originalModel).parts.stream()
                            .map(p -> p.getOverrides().handleItemState(p, stack, world, entity))
                            .collect(Collectors.toList()), this));
                } catch (ExecutionException e) {
                    return originalModel; // can this happen?
                }
            }

        }

    }

}
