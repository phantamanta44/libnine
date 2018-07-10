package io.github.phantamanta44.libnine.util.render;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.render.shader.IShader;
import io.github.phantamanta44.libnine.util.render.shader.IShaderProgram;
import io.github.phantamanta44.libnine.util.render.shader.ShaderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

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
                return null;
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
        private int[] shaderIds;
        private int programId;

        ShaderProgramImpl() {
            this.shaderSources = new LinkedList<>();
            this.shaderIds = null;
            this.programId = -1;
        }

        @Override
        public IShaderProgram withShader(IShader shader) {
            if (programId != -1) throw new UnsupportedOperationException("Shader program already compiled!");
            shaderSources.add(shader);
            return this;
        }

        @Override
        public IShaderProgram compile() {
            programId = GL20.glCreateProgram();
            this.shaderIds = new int[shaderSources.size()];
            for (int i = 0; i < shaderSources.size(); i++) {
                shaderIds[i] = GL20.glCreateShader(shaderSources.get(i).getType().getGlConstant());
                GL20.glShaderSource(shaderIds[i], shaderSources.get(i).getSource());
                GL20.glCompileShader(shaderIds[i]);
                if (GL20.glGetShaderi(shaderIds[i], GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                    LibNine.LOGGER.error("Compilation failed for one or more shaders! Ignoring...\n{}",
                            GL20.glGetShaderInfoLog(shaderIds[i], GL20.glGetShaderi(shaderIds[i], GL20.GL_INFO_LOG_LENGTH)));
                } else {
                    GL20.glAttachShader(programId, shaderIds[i]);
                }
            }
            GL20.glLinkProgram(programId);
            validShaders.add(this);
            return this;
        }

        @Override
        public void use() {
            if (programId == 0) throw new IllegalStateException("Shader program not compiled!");
            GL20.glUseProgram(programId);
        }

        private void clean() {
            for (int shaderId : shaderIds) GL20.glDeleteShader(shaderId);
            GL20.glDeleteProgram(programId);
            programId = 0;
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
