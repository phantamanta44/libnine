package io.github.phantamanta44.libnine.util.tuple;

import io.github.phantamanta44.libnine.util.function.IQuadConsumer;
import io.github.phantamanta44.libnine.util.function.IQuadFunction;

public interface IQuadruple<A, B, C, D> {

    A getA();

    B getB();

    C getC();

    D getD();

    default ITriple<B, C, D> truncateA() {
        return ITriple.of(getB(), getC(), getD());
    }

    default ITriple<A, C, D> truncateB() {
        return ITriple.of(getA(), getC(), getD());
    }

    default ITriple<A, B, D> truncateC() {
        return ITriple.of(getA(), getB(), getD());
    }

    default ITriple<A, B, C> truncateD() {
        return ITriple.of(getA(), getB(), getC());
    }

    default void sprexec(IQuadConsumer<A, B, C, D> executor) {
        executor.accept(getA(), getB(), getC(), getD());
    }

    default <T> T sprcall(IQuadFunction<A, B, C, D, T> executor) {
        return executor.apply(getA(), getB(), getC(), getD());
    }

    static <A, B, C, D> IQuadruple<A, B, C, D> of(A a, B b, C c, D d) {
        return new Impl<>(a, b, c, d);
    }

    class Impl<A, B, C, D> implements IQuadruple<A, B, C, D> {

        private final A a;
        private final B b;
        private final C c;
        private final D d;

        private Impl(A a, B b, C c, D d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
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

        @Override
        public D getD() {
            return d;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IQuadruple)) return false;
            IQuadruple quad = (IQuadruple)o;
            return quad.getA().equals(a) && quad.getB().equals(b)
                    && quad.getC().equals(c) && quad.getD().equals(d);
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode() ^ c.hashCode() & d.hashCode();
        }

    }

}
