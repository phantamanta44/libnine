package io.github.phantamanta44.libnine.util.render.shader;

public interface IShaderProgram {

    IShaderProgram withShader(IShader shader);

    IShaderProgram compile();

    void use();

}
