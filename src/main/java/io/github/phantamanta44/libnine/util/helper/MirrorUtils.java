package io.github.phantamanta44.libnine.util.helper;

import java.util.LinkedList;
import java.util.List;

public class MirrorUtils {

    public static <T> List<Class<? super T>> getHierarchy(Class<T> clazz) {
        List<Class<? super T>> result = new LinkedList<>();
        Class<? super T> current = clazz;
        while (current != null) {
            result.add(current);
            current = current.getSuperclass();
        }
        return result;
    }

}
