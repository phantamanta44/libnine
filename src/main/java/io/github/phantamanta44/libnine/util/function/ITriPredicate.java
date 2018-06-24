package io.github.phantamanta44.libnine.util.function;

@FunctionalInterface
public interface ITriPredicate<A, B, C> {

    boolean test(A a, B b, C c);

    default ITriPredicate<A, B, C> and(ITriPredicate<A, B, C> o) {
        return (a, b, c) -> test(a, b, c) && o.test(a, b, c);
    }

    default ITriPredicate<A, B, C> pre(ITriPredicate<A, B, C> o) {
        return o.and(this);
    }

}
