package io.github.phantamanta44.libnine.util.render.shader;

import org.lwjgl.opengl.GL20;

public enum ShaderType {

    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER);

    private final int glConstant;

    ShaderType(int glConstant) {
        this.glConstant = glConstant;
    }

    public int getGlConstant() {
        return glConstant;
    }

}
