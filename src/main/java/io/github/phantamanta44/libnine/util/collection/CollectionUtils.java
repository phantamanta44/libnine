package io.github.phantamanta44.libnine.util.collection;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {

    public static <K, V> Map<K, V> newMapOrEnumMap(Class<K> keyClass) {
        return keyClass.isEnum() ? newEnumMapUnchecked(keyClass) : new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static <K, V, E extends Enum<E>> Map<K, V> newEnumMapUnchecked(Class<K> keyClass) {
        return (Map<K, V>)new EnumMap<>((Class<E>)keyClass);
    }

}
