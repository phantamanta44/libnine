package io.github.phantamanta44.libnine.potion;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.util.Collections;
import java.util.List;

public class PotionUndispellable extends Potion {

    public PotionUndispellable(boolean debuff, int colour) {
        super(debuff, colour);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

}
