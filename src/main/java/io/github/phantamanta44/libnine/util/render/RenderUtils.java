package io.github.phantamanta44.libnine.util.render;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.render.shader.IShader;
import io.github.phantamanta44.libnine.util.render.shader.IShaderProgram;
import io.github.phantamanta44.libnine.util.render.shader.ShaderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RenderUtils {

    /*
     * Resource manager reload hook
     */

    public static void registerReloadHook() {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(manager -> reloadShaders());
    }

    /*
     * Optifine check
     */

    private static OptifineStatus optifined = OptifineStatus.CHECK_NEEDED;

    private static boolean checkOptifine() {
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

    /*
     * Shader stuff
     */

    public static IShader newShader(ShaderType type, Supplier<String> src) {
        return checkOptifine() ? NoopShader.INSTANCE : new ShaderImpl(type, src);
    }

    public static IShader newShader(ShaderType type, String src) {
        return newShader(type, () -> src);
    }

    public static IShader newShader(ShaderType type, ResourceLocation resource) {
        return newShader(type, () -> {
            try {
                return ResourceUtils.getAsString(resource);
            } catch (IOException e) {
                LibNine.LOGGER.error("Could not retrieve shader {}!", resource);
                LibNine.LOGGER.error(e);
                return "";
            }
        });
    }

    public static IShaderProgram newShaderProgram() {
        return checkOptifine() ? NoopShaderProgram.INSTANCE : new ShaderProgramImpl();
    }

    public static void clearShaderProgram() {
        if (checkOptifine()) return;
        GL20.glUseProgram(0);
    }

    public static void reloadShaders() {
        ShaderProgramImpl.reloadShaders();
    }

    private static class ShaderImpl implements IShader {

        private final ShaderType type;
        private final Supplier<String> source;

        ShaderImpl(ShaderType type, Supplier<String> source) {
            this.type = type;
            this.source = source;
        }

        @Override
        public ShaderType getType() {
            return type;
        }

        @Override
        public String getSource() {
            return source.get();
        }

    }

    private static class NoopShader implements IShader {

        static final NoopShader INSTANCE = new NoopShader();

        @Override
        public ShaderType getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSource() {
            throw new UnsupportedOperationException();
        }

    }

    private static class ShaderProgramImpl implements IShaderProgram {

        private static final Set<ShaderProgramImpl> validShaders = new HashSet<>();

        static void reloadShaders() {
            validShaders.forEach(s -> {
                s.clean();
                s.compile();
            });
        }

        private final List<IShader> shaderSources;
        @Nullable
        private int[] shaderIds;
        private int programId;

        ShaderProgramImpl() {
            this.shaderSources = new LinkedList<>();
            this.shaderIds = null;
            this.programId = -1;
        }

        @Override
        public IShaderProgram withShader(IShader shader) {
            if (shaderIds != null) throw new UnsupportedOperationException("Shader program already compiled!");
            shaderSources.add(shader);
            return this;
        }

        @Override
        public IShaderProgram compile() {
            programId = GL20.glCreateProgram();
            shaderIds = new int[shaderSources.size()];
            for (int i = 0; i < shaderSources.size(); i++) {
                String source = shaderSources.get(i).getSource();
                if (!source.isEmpty()) {
                    shaderIds[i] = GL20.glCreateShader(shaderSources.get(i).getType().getGlConstant());
                    GL20.glShaderSource(shaderIds[i], source);
                    GL20.glCompileShader(shaderIds[i]);
                    if (GL20.glGetShaderi(shaderIds[i], GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                        LibNine.LOGGER.error("Compilation failed for one or more shaders! Ignoring...\n{}",
                                GL20.glGetShaderInfoLog(shaderIds[i], GL20.glGetShaderi(shaderIds[i], GL20.GL_INFO_LOG_LENGTH)));
                    } else {
                        GL20.glAttachShader(programId, shaderIds[i]);
                    }
                }
            }
            GL20.glLinkProgram(programId);
            validShaders.add(this);
            return this;
        }

        @Override
        public void use() {
            if (shaderIds == null) throw new IllegalStateException("Shader program not compiled!");
            GL20.glUseProgram(programId);
        }

        private void clean() {
            if (shaderIds != null) {
                for (int shaderId : shaderIds) GL20.glDeleteShader(shaderId);
                GL20.glDeleteProgram(programId);
                programId = 0;
            }
        }

    }

    private static class NoopShaderProgram implements IShaderProgram {

        static final NoopShaderProgram INSTANCE = new NoopShaderProgram();

        @Override
        public IShaderProgram withShader(IShader shader) {
            return INSTANCE;
        }

        @Override
        public IShaderProgram compile() {
            return INSTANCE;
        }

        @Override
        public void use() {
            // NO-OP
        }

    }

}
