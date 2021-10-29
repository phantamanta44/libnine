package io.github.phantamanta44.libnine.util.function;

import javax.annotation.Nullable;

@FunctionalInterface
public interface INullableSupplier<T> {

    @Nullable
    T get();

}
