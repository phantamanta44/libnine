package io.github.phantamanta44.libnine.util.tuple;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface IPair<A, B> {

    A getA();

    B getB();

    default <T> ITriple<T, A, B> promoteA(T value) {
        return ITriple.of(value, getA(), getB());
    }
    
    default <T> ITriple<A, T, B> promoteB(T value) {
        return ITriple.of(getA(), value, getB());
    }

    default <T> ITriple<A, B, T> promoteC(T value) {
        return ITriple.of(getA(), getB(), value);
    }

    default void sprexec(BiConsumer<A, B> executor) {
        executor.accept(getA(), getB());
    }
    
    default <T> T sprcall(BiFunction<A, B, T> executor) {
        return executor.apply(getA(), getB());
    }

    static <A, B> IPair<A, B> of(A a, B b) {
        return new Impl<>(a, b);
    }

    class Impl<A, B> implements IPair<A, B> {

        private final A a;
        private final B b;

        private Impl(A a, B b) {
            this.a = a;
            this.b = b;
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
        public boolean equals(Object o) {
            if (!(o instanceof IPair)) return false;
            IPair pair = (IPair)o;
            return pair.getA().equals(a) && pair.getB().equals(b);
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }

    }

}
