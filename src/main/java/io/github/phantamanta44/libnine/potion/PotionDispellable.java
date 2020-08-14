package io.github.phantamanta44.libnine.potion;

import net.minecraft.potion.Potion;

// needed because the potion constructors are protected
public class PotionDispellable extends Potion {

    public PotionDispellable(boolean debuff, int colour) {
        super(debuff, colour);
    }

}
