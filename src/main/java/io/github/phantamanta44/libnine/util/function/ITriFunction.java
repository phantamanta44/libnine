package io.github.phantamanta44.libnine.util.function;

@FunctionalInterface
public interface ITriFunction<A, B, C, T> {

    T apply(A a, B b, C c);

}
