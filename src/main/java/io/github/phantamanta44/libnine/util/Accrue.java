package io.github.phantamanta44.libnine.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class Accrue<T> implements Consumer<T> {

    private final Collection<T> backing;

    public Accrue(Collection<T> backing) {
        this.backing = backing;
    }

    @Override
    public void accept(T t) {
        backing.add(t);
    }

    public void acceptAll(T... values) {
        backing.addAll(Arrays.asList(values));
    }

    public void acceptAll(Collection<T> values) {
        backing.addAll(values);
    }

}
