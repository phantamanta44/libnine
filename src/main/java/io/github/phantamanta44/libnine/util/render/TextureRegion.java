package io.github.phantamanta44.libnine.util.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class TextureRegion {

    private final TextureResource texture;
    private final int x, y, width, height;
    private final float u1, v1, u2, v2;
    private final float du, dv;

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
        this.du = this.u2 - this.u1;
        this.dv = this.v2 - this.v1;
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

    public float getUDifferential() {
        return du;
    }

    public float getVDifferential() {
        return dv;
    }

    public void draw(int x, int y, int width, int height) {
        texture.bind();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + height, 0D).tex(u1, v2).endVertex();
        buf.pos(x + width, y + height, 0D).tex(u2, v2).endVertex();
        buf.pos(x + width, y, 0D).tex(u2, v1).endVertex();
        buf.pos(x, y, 0D).tex(u1, v1).endVertex();
        tess.draw();
    }

    public void drawPartial(int x, int y, int width, int height, float x1, float y1, float x2, float y2) {
        float xStart = x + width * x1, xEnd = x + width * x2;
        float yStart = y + height * y1, yEnd = y + height * y2;
        float uStart = u1 + du * x1, uEnd = u1 + du * x2;
        float vStart = v1 + dv * y1, vEnd = v1 + dv * y2;
        texture.bind();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(xStart, yEnd, 0D).tex(uStart, vEnd).endVertex();
        buf.pos(xEnd, yEnd, 0D).tex(uEnd, vEnd).endVertex();
        buf.pos(xEnd, yStart, 0D).tex(uEnd, vStart).endVertex();
        buf.pos(xStart, yStart, 0D).tex(uStart, vStart).endVertex();
        tess.draw();
    }

}
