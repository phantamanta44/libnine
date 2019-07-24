package io.github.phantamanta44.libnine.util.render.shader;

public interface IShaderProgram {

    IShaderProgram use();

    <T> IShaderProgram setUniform(Uniform<T, ?> uniform, T value);

    interface Source {

        Source withShader(IShader shader);

        <T> Uniform<T, ?> getUniform(UniformType<T, ?> type, String name);

        IShaderProgram compile();

    }

}
