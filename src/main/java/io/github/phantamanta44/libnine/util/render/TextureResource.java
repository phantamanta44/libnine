package io.github.phantamanta44.libnine.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class TextureResource {

    private final ResourceLocation texture;
    private final int width, height;
    private final TextureRegion fullRegion;

    public TextureResource(ResourceLocation texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.fullRegion = new TextureRegion(this);
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void bind() {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public TextureRegion getRegion(int x, int y, int width, int height) {
        return new TextureRegion(this, x, y, width, height);
    }

    public TextureRegion asTexture() {
        return fullRegion;
    }

}
