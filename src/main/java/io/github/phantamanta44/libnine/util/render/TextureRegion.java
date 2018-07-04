package io.github.phantamanta44.libnine.util.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TextureRegion {

    private final TextureResource texture;
    private final int x, y, width, height;
    private final float u1, v1, u2, v2;

    public TextureRegion(TextureResource texture, int x, int y, int width, int height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u1 = (float)x / texture.getWidth();
        this.v1 = (float)y / texture.getHeight();
        this.u2 = this.u1 + (float)width / texture.getWidth();
        this.v2 = this.v1 + (float)height / texture.getHeight();
    }

    public TextureRegion(TextureResource texture) {
        this(texture, 0, 0, 1, 1);
    }
    
    public TextureResource getTexture() {
        return texture;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getU1() {
        return u1;
    }

    public float getV1() {
        return v1;
    }

    public float getU2() {
        return u2;
    }

    public float getV2() {
        return v2;
    }

    public void draw(int x, int y, int width, int height) {
        texture.bind();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + height, 0D).tex(u1, v2).endVertex();
        buf.pos(x + width, y + height, 0D).tex(u2, v2).endVertex();
        buf.pos(x + width, y, 0D).tex(u2, v1).endVertex();
        buf.pos(x, y, 0D).tex(u1, v1).endVertex();
        tess.draw();
    }

}
