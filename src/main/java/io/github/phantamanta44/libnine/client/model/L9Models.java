package io.github.phantamanta44.libnine.client.model;

import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;

public class L9Models {

    public static void registerModels() {
        ModelLoaderRegistry.registerLoader(new ParameterizedItemModelLoader());
        FMLClientHandler.instance().refreshResources(VanillaResourceType.MODELS);
    }

    static boolean isOfType(ResourceLocation resource, String type) {
        try {
            JsonObject model = ResourceUtils.getAsJson(resource).getAsJsonObject();
            return model.has("9s") && model.get("9s").getAsString().equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public static ResourceLocation getRealModelLocation(ResourceLocation resource) {

        return new ResourceLocation(resource.getNamespace(), (resource.getPath().startsWith("models/")
                ? resource.getPath() : ("models/" + resource.getPath())) + ".json");
    }

}
