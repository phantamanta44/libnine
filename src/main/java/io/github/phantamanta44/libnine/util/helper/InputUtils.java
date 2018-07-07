package io.github.phantamanta44.libnine.util.helper;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class InputUtils {

    public static String getKeybindKeyName(KeyBinding binding) {
        return Keyboard.getKeyName(binding.getKeyCode());
    }

    public static boolean checkModsNonExclusive(ModKey... mods) {
        for (ModKey mod : mods) {
            if (!mod.isActive()) return false;
        }
        return true;
    }

    public static boolean checkModsExclusive(ModKey... mods) {
        return ModKey.calculateMask(mods) == ModKey.getMask();
    }

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
            for (ModKey mod : ModKey.values()) {
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

}
