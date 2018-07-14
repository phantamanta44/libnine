package io.github.phantamanta44.libnine.recipe.output;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemStackOutput implements IRcpOut<ItemStack> {

    private final ItemStack stack;

    public ItemStackOutput(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getOutput() {
        return stack;
    }

    @Override
    public boolean isAcceptable(ItemStack stack) {
        return (stack == ItemStack.EMPTY || (ItemHandlerHelper.canItemStacksStack(stack, this.stack)
                && stack.getCount() + this.stack.getCount() <= stack.getMaxStackSize()));
    }

}
