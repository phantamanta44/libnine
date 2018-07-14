package io.github.phantamanta44.libnine.recipe.input;

public interface IRcpIn<T> {

    boolean matches(T input);

    T consume(T input);

}
