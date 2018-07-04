package io.github.phantamanta44.libnine.util.function;

@FunctionalInterface
public interface IQuadConsumer<A, B, C, D> {

    void accept(A a, B b, C c, D d);

}
