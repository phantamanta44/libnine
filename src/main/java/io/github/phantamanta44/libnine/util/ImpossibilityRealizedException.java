package io.github.phantamanta44.libnine.util;

public class ImpossibilityRealizedException extends RuntimeException {

    public ImpossibilityRealizedException() {
        super("Reached impossible state!");
    }

    public ImpossibilityRealizedException(Throwable cause) {
        super("Reached impossible state!", cause);
    }

}
