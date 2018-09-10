package io.github.phantamanta44.libnine.util;

public enum TriBool {

    TRUE(true),
    FALSE(false),
    NONE(false);

    public final boolean value;

    TriBool(boolean value) {
        this.value = value;
    }

    public static TriBool wrap(boolean value) {
        return value ? TRUE : FALSE;
    }

}
