package io.github.phantamanta44.libnine.item;

import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.block.state.VirtualState;
import net.minecraft.item.ItemStack;

public class L9ItemBlockStated extends L9ItemBlock {

    private final L9BlockStated block;

    public L9ItemBlockStated(L9BlockStated block) {
        super(block);
        this.block = block;
        setHasSubtypes(true);
    }

    /*
     * Initializers
     */

    public String getModelName(VirtualState state) {
        return getBlock().getInternalName();
    }

    public String getModelVariant(VirtualState state) {
        return "inventory";
    }

    /*
     * Properties
     */

    @Override
    public L9BlockStated getBlock() {
        return block;
    }

    /*
     * Behaviour
     */

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + stack.getMetadata();
    }

}
