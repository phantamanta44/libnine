package io.github.phantamanta44.libnine.client.model;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class L9CachingModelLoader implements ICustomModelLoader {

    private final String modelType;
    private final Map<ResourceLocation, IModel> cache = new HashMap<>();

    public L9CachingModelLoader(String modelType) {
        this.modelType = modelType;
    }

    @Override
    public boolean accepts(ResourceLocation loc) {
        return L9Models.isOfType(L9Models.getRealModelLocation(loc), modelType);
    }

    @Override
    public IModel loadModel(ResourceLocation loc) throws Exception {
        IModel model = cache.get(loc);
        if (model != null) {
            return model;
        }

        try {
            model = parseModel(Objects.requireNonNull(L9Models.resolve(L9Models.getRealModelLocation(loc))).model);
        } catch (Exception e) {
            throw new ModelLoaderRegistry.LoaderException("Failed to load L9 model: " + loc, e);
        }
        cache.put(loc, model);
        return model;
    }

    public abstract IModel parseModel(JsonObject modelDto) throws Exception;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        cache.clear();
    }

}
