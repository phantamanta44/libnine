package io.github.phantamanta44.libnine.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public class L9CreativeTab extends CreativeTabs {

    private final LazyConstant<ItemStack> icon;

    public L9CreativeTab(String name, Supplier<ItemStack> iconFactory) {
        super(name);
        this.icon = new LazyConstant<>(iconFactory);
    }

    @Override
    public ItemStack createIcon() {
        return icon.get();
    }

}
