package io.github.phantamanta44.libnine.util.render.shader;

import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;

public class Uniform<T, C> {

    private final UniformType<T, C> type;
    private final String name;
    @Nullable
    private final C context;

    public Uniform(UniformType<T, C> type, String name) {
        this.type = type;
        this.name = name;
        this.context = type.generateContext();
    }

    public UniformType<T, C> getType() {
        return type;
    }

    public int computeLocation(int programId) {
        return GL20.glGetUniformLocation(programId, name);
    }

    @Nullable
    public C getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Uniform && name.equals(((Uniform)obj).name);
    }

}
