package io.github.phantamanta44.libnine.util.render;

import io.github.phantamanta44.libnine.LibNine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    /*
     * Optifine check
     */

    private static OptifineStatus optifined = OptifineStatus.CHECK_NEEDED;

    public static boolean checkOptifine() {
        if (optifined == OptifineStatus.CHECK_NEEDED) {
            optifined = Loader.isModLoaded("optifine") ? OptifineStatus.PRESENT : OptifineStatus.NOT_PRESENT;
            if (optifined == OptifineStatus.PRESENT) {
                LibNine.LOGGER.warn("Whoops! You have Optifine installed. That might cause some weird rendering issues.");
            }
        }
        return optifined == OptifineStatus.PRESENT;
    }

    private enum OptifineStatus {

        CHECK_NEEDED, PRESENT, NOT_PRESENT

    }

    /*
     * Lightmap stuff
     */

    private static boolean lightmapCached = false;
    private static float cachedLightmapX, cachedLightmapY;

    public static void setLightmapCoords(float x, float y) {
        if (checkOptifine()) return;
        if (!lightmapCached) cacheLightmapCoords(OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, x, y);
    }

    private static void cacheLightmapCoords(float x, float y) {
        cachedLightmapX = x;
        cachedLightmapY = y;
        GlStateManager.disableLighting();
        lightmapCached = true;
    }

    public static void restoreLightmap() {
        if (checkOptifine()) return;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, cachedLightmapX, cachedLightmapY);
        GlStateManager.enableLighting();
        lightmapCached = false;
    }

    public static void enableFullBrightness() {
        setLightmapCoords(240F, 240F);
    }

    /*
     * Misc utils
     */

    public static Vec3d getInterpPos(Entity entity, float partialTicks) {
        return new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    }

    /*
     * Renders
     */

    public static void renderWorldOrtho(double x, double y, double z,
                                        float scaleX, float scaleY, float angle, float u1, float v1, float u2, float v2,
                                        float lookX, float lookXZ, float lookZ, float lookYZ, float lookXY) {
        Vec3d[] vertices = new Vec3d[] {
                new Vec3d(-lookX * scaleX - lookYZ * scaleY, -lookXZ * scaleY, -lookZ * scaleX - lookXY * scaleY),
                new Vec3d(-lookX * scaleX + lookYZ * scaleY, lookXZ * scaleY, -lookZ * scaleX + lookXY * scaleY),
                new Vec3d(lookX * scaleX + lookYZ * scaleY, lookXZ * scaleY, lookZ * scaleX + lookXY * scaleY),
                new Vec3d(lookX * scaleX - lookYZ * scaleY, -lookXZ * scaleY, lookZ * scaleX - lookXY * scaleY)
        };
        float angX = MathHelper.cos(angle * 0.5F);
        Vec3d axial = Minecraft.getMinecraft().player.getLookVec().scale(MathHelper.sin(angle * 0.5F));
        for (int i = 0; i < 4; ++i) {
            vertices[i] = axial.scale(2D * vertices[i].dotProduct(axial))
                    .add(vertices[i].scale(angX * angX - axial.dotProduct(axial)))
                    .add(axial.crossProduct(vertices[i]).scale(2F * angX));
        }
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x + vertices[0].x, y + vertices[0].y, z + vertices[0].z).tex(u2, v2).endVertex();
        buf.pos(x + vertices[1].x, y + vertices[1].y, z + vertices[1].z).tex(u2, v1).endVertex();
        buf.pos(x + vertices[2].x, y + vertices[2].y, z + vertices[2].z).tex(u1, v1).endVertex();
        buf.pos(x + vertices[3].x, y + vertices[3].y, z + vertices[3].z).tex(u1, v2).endVertex();
        tess.draw();
    }

    public static void renderWorldOrtho(double x, double y, double z, float scaleX, float scaleY, float angle,
                                        float lookX, float lookXZ, float lookZ, float lookYZ, float lookXY) {
        renderWorldOrtho(x, y, z, scaleX, scaleY, angle, 0F, 0F, 1F, 1F, lookX, lookXZ, lookZ, lookYZ, lookXY);
    }

    public static void renderWorldOrtho(double x, double y, double z,
                                        float scaleX, float scaleY, float angle, float u1, float v1, float u2, float v2) {
        renderWorldOrtho(x, y, z, scaleX, scaleY, angle, u1, v1, u2, v2,
                ActiveRenderInfo.getRotationX(), ActiveRenderInfo.getRotationXZ(), ActiveRenderInfo.getRotationZ(),
                ActiveRenderInfo.getRotationYZ(), ActiveRenderInfo.getRotationXY());
    }

    public static void renderWorldOrtho(double x, double y, double z,
                                        float scaleX, float scaleY, float angle, TextureRegion icon) {
        icon.getTexture().bind();
        renderWorldOrtho(x, y, z, scaleX, scaleY, angle, icon.getU1(), icon.getV1(), icon.getU2(), icon.getV2());
    }

    public static void renderWorldOrtho(double x, double y, double z, float scaleX, float scaleY, float angle) {
        renderWorldOrtho(x, y, z, scaleX, scaleY, angle, 0F, 0F, 1F, 1F);
    }

    public static void renderScreenOverlay(TextureRegion texture, ScaledResolution res) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        texture.draw(0, 0, res.getScaledWidth(), res.getScaledHeight());
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }

}
