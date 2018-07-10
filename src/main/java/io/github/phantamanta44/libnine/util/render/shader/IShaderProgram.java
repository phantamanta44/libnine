package io.github.phantamanta44.libnine.util.render.shader;

import io.github.phantamanta44.libnine.util.render.RenderUtils;

public interface IShaderProgram {

    IShaderProgram withShader(IShader shader);

    IShaderProgram compile();

    void use();

}
