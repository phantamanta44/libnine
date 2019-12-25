package io.github.phantamanta44.libnine.util.tuple;

import io.github.phantamanta44.libnine.util.function.ITriConsumer;
import io.github.phantamanta44.libnine.util.function.ITriFunction;

public interface ITriple<A, B, C> {

    A getA();

    B getB();

    C getC();

    default IPair<B, C> truncateA() {
        return IPair.of(getB(), getC());
    }

    default IPair<A, C> truncateB() {
        return IPair.of(getA(), getC());
    }

    default IPair<A, B> truncateC() {
        return IPair.of(getA(), getB());
    }

    default void sprexec(ITriConsumer<A, B, C> executor) {
        executor.accept(getA(), getB(), getC());
    }

    default <T> T sprcall(ITriFunction<A, B, C, T> executor) {
        return executor.apply(getA(), getB(), getC());
    }

    static <A, B, C> ITriple<A, B, C> of(A a, B b, C c) {
        return new Impl<>(a, b, c);
    }

    class Impl<A, B, C> implements ITriple<A, B, C> {

        private final A a;
        private final B b;
        private final C c;

        private Impl(A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public A getA() {
            return a;
        }

        @Override
        public B getB() {
            return b;
        }

        @Override
        public C getC() {
            return c;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ITriple)) return false;
            ITriple tri = (ITriple)o;
            return tri.getA().equals(a) && tri.getB().equals(b) && tri.getC().equals(c);
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode() ^ c.hashCode();
        }

    }

}
