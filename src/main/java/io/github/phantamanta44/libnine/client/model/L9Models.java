package io.github.phantamanta44.libnine.client.model;

import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class L9Models {

    public static void registerModels() {
        ModelLoaderRegistry.registerLoader(new ParameterizedItemModelLoader());
        Minecraft.getMinecraft().refreshResources();
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

        return new ResourceLocation(resource.getResourceDomain(), (resource.getResourcePath().startsWith("models/")
                ? resource.getResourcePath() : ("models/" + resource.getResourcePath())) + ".json");
    }

}
