package io.github.phantamanta44.libnine.util.helper;

import net.minecraft.item.ItemStack;

public class ItemUtils {

    public static ItemStack copyStack(ItemStack base, int count) {
        ItemStack copy = base.copy();
        copy.setCount(count);
        return copy;
    }

}
