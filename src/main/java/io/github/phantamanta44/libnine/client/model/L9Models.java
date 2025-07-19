package io.github.phantamanta44.libnine.client.model;

import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class L9Models {

    private static final ModelCache cache = new ModelCache();

    public static void registerModels() {
        ModelLoaderRegistry.registerLoader(new ParameterizedItemModelLoader());
        ModelLoaderRegistry.registerLoader(new SumItemModel.Loader());
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(cache);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(cache);
    }

    @Nullable
    public static ModelData resolve(ResourceLocation resource) throws IOException {
        return cache.resolve(resource);
    }

    public static boolean isOfType(ResourceLocation resource, String type) {
        try {
            ModelData data = resolve(resource);
            return data != null && data.type.equals(type);
        } catch (Exception e) {
            LibNine.LOGGER.warn("Failed to read model data: " + resource, e);
            return false;
        }
    }

    public static ResourceLocation getRealModelLocation(ResourceLocation resource) {
        return new ResourceLocation(resource.getNamespace(), (resource.getPath().startsWith("models/")
                ? resource.getPath() : ("models/" + resource.getPath())) + ".json");
    }

    public static class ModelData {

        public final String type;
        public final JsonObject model;

        public ModelData(String type, JsonObject model) {
            this.type = type;
            this.model = model;
        }

    }

    // Loosely based on the CTM model loader from the chisel team, licensed GPL 2.0
    // team.chisel.ctm.client.model.parsing.ModelLoaderCTM
    private static class ModelCache implements ISelectiveResourceReloadListener {

        private final Map<ResourceLocation, Optional<ModelData>> cache = new HashMap<>();

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> predicate) {
            if (predicate.test(VanillaResourceType.MODELS)) {
                cache.clear();
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onModelsBaked(ModelBakeEvent event) {
            cache.clear(); // any L9 model loaders should have cached all their models by now
        }

        @Nullable
        public ModelData resolve(ResourceLocation loc) throws IOException {
            Optional<ModelData> dataMaybe = cache.get(loc);
            if (dataMaybe != null) {
                return dataMaybe.orElse(null);
            }

            try {
                JsonObject modelDto = ResourceUtils.getAsJson(loc).getAsJsonObject();
                if (modelDto.has("9s")) {
                    ModelData data = new ModelData(modelDto.get("9s").getAsString(), modelDto);
                    cache.put(loc, Optional.of(data));
                    return data;
                }
            } catch (FileNotFoundException e) {
                // NO-OP
            }
            cache.put(loc, Optional.empty());
            return null;
        }

    }

}
