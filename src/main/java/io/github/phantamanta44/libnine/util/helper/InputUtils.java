package io.github.phantamanta44.libnine.util.helper;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class InputUtils {

    public static String getKeybindKeyName(KeyBinding binding) {
        return Keyboard.getKeyName(binding.getKeyCode());
    }

}
