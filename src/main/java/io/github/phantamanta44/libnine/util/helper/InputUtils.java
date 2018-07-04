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

}
