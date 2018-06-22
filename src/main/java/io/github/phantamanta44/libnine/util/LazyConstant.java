package io.github.phantamanta44.libnine.util;

import java.util.function.Supplier;

public class LazyConstant<T> {

    private final Supplier<T> factory;
    private T value;

    public LazyConstant(Supplier<T> factory) {
        this.factory = factory;
    }

    public T get() {
        return value != null ? value : (value = factory.get());
    }

}
