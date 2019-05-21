package io.github.phantamanta44.libnine.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IDisplayableMatcher<T> extends Predicate<T> {

    List<T> getVisuals();

    default T getVisual() {
        return getVisuals().get(0);
    }

    static <T> IDisplayableMatcher<T> ofMany(Supplier<List<T>> display, Predicate<T> matcher) {
        return new IDisplayableMatcher<T>() {
            @Override
            public List<T> getVisuals() {
                return display.get();
            }

            @Override
            public boolean test(T t) {
                return matcher.test(t);
            }
        };
    }

    static <T> IDisplayableMatcher<T> of(Supplier<T> display, Predicate<T> matcher) {
        return ofMany(() -> Collections.singletonList(display.get()), matcher);
    }

}
