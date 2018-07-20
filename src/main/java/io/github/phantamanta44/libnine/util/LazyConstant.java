package io.github.phantamanta44.libnine.util;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class LazyConstant<T> {

    private final Supplier<T> factory;
    @Nullable
    private T value;

    public LazyConstant(Supplier<T> factory) {
        this.factory = factory;
    }

    public T get() {
        return value != null ? value : (value = factory.get());
    }

}
