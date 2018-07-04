package io.github.phantamanta44.libnine.item;

import io.github.phantamanta44.libnine.LibNine;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class L9ItemSubs extends L9Item {

    private final int variantCount;

    public L9ItemSubs(String name, int variantCount) {
        super(name);
        setHasSubtypes(true);
        this.variantCount = variantCount;
    }

    /*
     * Initializers
     */

    @Override
    protected void initModel() {
        for (int i = 0; i < variantCount; i++) {
            LibNine.PROXY.getRegistrar().queueItemModelReg(this, i, getModelName(i));
        }
    }

    protected String getModelName(int variant) {
        return getInternalName();
    }

    /*
     * Properties
     */

    public int getVariantCount() {
        return variantCount;
    }

    /*
     * Behaviour
     */

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < variantCount; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + stack.getMetadata();
    }

}
