package io.github.phantamanta44.libnine.util.render.shader;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import io.github.phantamanta44.libnine.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ShaderUtils {

    private static final Set<ShaderProgramImpl> validShaders = new HashSet<>();

    public static void registerReloadHook() {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(manager -> reloadShaders());
    }

    public static IShader newShader(ShaderType type, Supplier<String> src) {
        return RenderUtils.checkOptifine() ? NoopShader.INSTANCE : new ShaderImpl(type, src);
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

    public static IShaderProgram.Source newShaderProgram() {
        return RenderUtils.checkOptifine() ? NoopShaderProgramSource.INSTANCE : new ShaderProgramSourceImpl();
    }

    public static void clearShaderProgram() {
        if (RenderUtils.checkOptifine()) return;
        GL20.glUseProgram(0);
    }

    public static void reloadShaders() {
        validShaders.forEach(s -> {
            s.clean();
            s.compile();
        });
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

        private final List<IShader> shaderSources;
        private final List<Uniform<?, ?>> uniforms;
        private int[] shaderIds;
        private int programId = -1;
        private final TObjectIntMap<Uniform<?, ?>> unifMapping = new TObjectIntHashMap<>();

        ShaderProgramImpl(List<IShader> shaderSources, List<Uniform<?, ?>> uniforms) {
            this.shaderSources = shaderSources;
            this.uniforms = uniforms;
            this.shaderIds = new int[shaderSources.size()];
        }

        @Override
        public IShaderProgram use() {
            if (programId != -1) {
                GL20.glUseProgram(programId);
            }
            return this;
        }

        @Override
        public <T> IShaderProgram setUniform(Uniform<T, ?> uniform, T value) {
            if (programId != -1) {
                setUniform0(uniform, unifMapping.get(uniform), value);
            }
            return this;
        }

        private static <T, C> void setUniform0(Uniform<T, C> uniform, int location, T value) {
            uniform.getType().set(location, value, uniform.getContext());
        }

        void compile() {
            programId = GL20.glCreateProgram();
            for (int i = 0; i < shaderSources.size(); i++) {
                String source = shaderSources.get(i).getSource();
                if (!source.isEmpty()) {
                    int shaderId = shaderIds[i] = GL20.glCreateShader(shaderSources.get(i).getType().getGlConstant());
                    GL20.glShaderSource(shaderId, source);
                    GL20.glCompileShader(shaderId);
                    if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                        LibNine.LOGGER.warn("Compilation failed for one or more shaders! Ignoring...\n{}",
                                GL20.glGetShaderInfoLog(shaderId, GL20.glGetShaderi(shaderId, GL20.GL_INFO_LOG_LENGTH)));
                    } else {
                        GL20.glAttachShader(programId, shaderId);
                    }
                }
            }
            GL20.glLinkProgram(programId);
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                LibNine.LOGGER.warn("Compilation failed for shader program! Ignoring...\n{}",
                        GL20.glGetProgramInfoLog(programId, GL20.glGetProgrami(programId, GL20.GL_INFO_LOG_LENGTH)));
                programId = -1;
            } else {
                for (Uniform<?, ?> unif : uniforms) {
                    unifMapping.put(unif, unif.computeLocation(programId));
                }
            }
        }

        void clean() {
            if (programId != -1) {
                for (int shaderId : shaderIds) GL20.glDeleteShader(shaderId);
                GL20.glDeleteProgram(programId);
                programId = -1;
                unifMapping.clear();
            }
        }

    }

    private static class ShaderProgramSourceImpl implements IShaderProgram.Source {

        private final List<IShader> shaderSources = new ArrayList<>();
        private final List<Uniform<?, ?>> uniforms = new ArrayList<>();

        @Override
        public IShaderProgram.Source withShader(IShader shader) {
            shaderSources.add(shader);
            return this;
        }

        @Override
        public <T> Uniform<T, ?> getUniform(UniformType<T, ?> type, String name) {
            Uniform<T, ?> uniform = new Uniform<>(type, name);
            uniforms.add(uniform);
            return uniform;
        }

        @Override
        public IShaderProgram compile() {
            ShaderProgramImpl program = new ShaderProgramImpl(shaderSources, uniforms);
            program.compile();
            validShaders.add(program);
            return program;
        }

    }

    private static class NoopShaderProgram implements IShaderProgram {

        static final NoopShaderProgram INSTANCE = new NoopShaderProgram();

        @Override
        public <T> IShaderProgram setUniform(Uniform<T, ?> uniform, T value) {
            return this;
        }

        @Override
        public IShaderProgram use() {
            return this;
        }

    }

    private static class NoopShaderProgramSource implements IShaderProgram.Source {

        static final NoopShaderProgramSource INSTANCE = new NoopShaderProgramSource();

        @Override
        public IShaderProgram.Source withShader(IShader shader) {
            return INSTANCE;
        }

        @Override
        public <T> Uniform<T, ?> getUniform(UniformType<T, ?> type, String name) {
            //noinspection unchecked
            return NoopUniform.INSTANCE;
        }

        @Override
        public IShaderProgram compile() {
            return NoopShaderProgram.INSTANCE;
        }

    }

    @SuppressWarnings("unchecked")
    private static class NoopUniform extends Uniform {

        private static final UniformType DUMMY_TYPE = new UniformType((l, v, c) -> {
            // NO-OP
        });
        static final NoopUniform INSTANCE = new NoopUniform();

        private NoopUniform() {
            super(DUMMY_TYPE, "<noop>");
        }

    }

}
