package io.github.phantamanta44.libnine.util.function;

@FunctionalInterface
public interface ITriConsumer<A, B, C> {

    void accept(A a, B b, C c);

}
