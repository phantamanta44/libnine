package io.github.phantamanta44.libnine.util.helper;

import org.lwjgl.input.Keyboard;

public enum ModKey {

    CTRL(Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL),
    SHIFT(Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT),
    ALT(Keyboard.KEY_LMENU, Keyboard.KEY_RMENU);

    private final int[] codes;

    ModKey(int... codes) {
        this.codes = codes;
    }

    public boolean isActive() {
        for (int code : codes) {
            if (Keyboard.isKeyDown(code)) return true;
        }
        return false;
    }

    public static int getMask() {
        int mask = 0;
        for (ModKey mod : values()) {
            if (mod.isActive()) mask |= 1 << mod.ordinal();
        }
        return mask;
    }

    public static int calculateMask(ModKey... mods) {
        int mask = 0;
        for (ModKey mod : mods) mask |= 1 << mod.ordinal();
        return mask;
    }

}
