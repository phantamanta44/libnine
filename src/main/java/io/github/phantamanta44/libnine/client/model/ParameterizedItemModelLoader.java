package io.github.phantamanta44.libnine.client.model;

import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ParameterizedItemModelLoader implements ICustomModelLoader {

    private final ResourceInjector resourceInjector;

    public ParameterizedItemModelLoader() {
        this.resourceInjector = new ResourceInjector();
        Minecraft.getMinecraft().defaultResourcePacks.add(this.resourceInjector);
    }

    @Override
    public boolean accepts(ResourceLocation resource) {
        return L9Models.isOfType(L9Models.getRealModelLocation(resource), "pi");
    }

    @Override
    public IModel loadModel(ResourceLocation resource) throws Exception {
        JsonObject model = ResourceUtils.getAsJson(L9Models.getRealModelLocation(resource)).getAsJsonObject();
        JsonObject archetype = model.getAsJsonObject("archetype");
        if (archetype == null) throw new NoSuchElementException("No archetype for PI model: " + resource);
        JsonObject mutations = model.getAsJsonObject("mutations");
        Table<String, String, JsonElement> mutationTable = null;
        if (mutations != null) {
            mutationTable = Tables.newCustomTable(new HashMap<>(), HashMap::new);
            for (Map.Entry<String, JsonElement> mKey : mutations.entrySet()) {
                for (Map.Entry<String, JsonElement> mValue : mKey.getValue().getAsJsonObject().entrySet()) {
                    mutationTable.put(mKey.getKey(), mValue.getKey(), mValue.getValue());
                }
            }
        }
        return new ParameterizedItemModel(resourceInjector, archetype, mutationTable);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        resourceInjector.flush();
    }

    static class ResourceInjector implements IResourcePack {

        private static final String RESOURCE_DOMAIN = "libnine_pi";
        private static final Set<String> RESOURCE_DOMAINS = Sets.newHashSet(RESOURCE_DOMAIN);

        private final Map<String, String> resources;

        private long resourceIndex;

        ResourceInjector() {
            this.resources = new HashMap<>();
            this.resourceIndex = 0;
        }

        @Override
        public InputStream getInputStream(ResourceLocation location) throws IOException {
            String resource = resources.get(location.getPath());
            if (resource == null) {
                throw new FileNotFoundException("Unknown PI resource: " + location.getPath());
            }
            return new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean resourceExists(ResourceLocation location) {
            return resources.containsKey(location.getPath());
        }

        @Override
        public Set<String> getResourceDomains() {
            return RESOURCE_DOMAINS;
        }

        @Nullable
        @Override
        public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) {
            return null;
        }

        @Override
        public BufferedImage getPackImage() throws IOException {
            return TextureUtil.readBufferedImage(getInputStream(new ResourceLocation("textures/misc/unknown_pack.png")));
        }

        @Override
        public String getPackName() {
            return "libnine magic pi pack";
        }

        ResourceLocation injectResource(String resource) {
            resources.put("models/" + Long.toString(resourceIndex) + ".json", resource);
            return new ResourceLocation(RESOURCE_DOMAIN, Long.toString(resourceIndex++));
        }

        void flush() {
            resources.clear();
        }

    }

}
