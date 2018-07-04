package io.github.phantamanta44.libnine.util.function;

@FunctionalInterface
public interface IQuadFunction<A, B, C, D, T> {

    T apply(A a, B b, C c, D d);

}
