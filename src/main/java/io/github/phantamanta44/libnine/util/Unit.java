package io.github.phantamanta44.libnine.util;

public class Unit {

    public static final Unit INSTANCE = new Unit();

    private Unit() {
        // NO-OP
    }

    @Override
    public int hashCode() {
        return 0xFEEDBEEF;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

}
