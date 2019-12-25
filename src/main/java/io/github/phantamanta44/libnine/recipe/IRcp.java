package io.github.phantamanta44.libnine.recipe;

import io.github.phantamanta44.libnine.recipe.input.IRcpIn;
import io.github.phantamanta44.libnine.recipe.output.IRcpOut;

public interface IRcp<T, I extends IRcpIn<T>, O extends IRcpOut<?>> {

    I input();

    O mapToOutput(T input);

}
