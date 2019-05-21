package io.github.phantamanta44.libnine.recipe.input;

import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import io.github.phantamanta44.libnine.util.helper.ItemUtils;
import net.minecraft.item.ItemStack;

public class ItemStackInput implements IRcpIn<ItemStack> {

    private final IDisplayableMatcher<ItemStack> matcher;
    private final int amount;

    public ItemStackInput(ItemStack stack) {
        this.matcher = ItemUtils.matchesWithWildcard(stack);
        this.amount = stack.getCount();
    }

    public ItemStackInput(IDisplayableMatcher<ItemStack> matcher, int amount) {
        this.matcher = matcher;
        this.amount = amount;
    }

    public IDisplayableMatcher<ItemStack> getMatcher() {
        return matcher;
    }

    @Override
    public boolean matches(ItemStack input) {
        return matcher.test(input) && input.getCount() >= amount;
    }

    @Override
    public ItemStack consume(ItemStack input) {
        if (input.getCount() == amount) return ItemStack.EMPTY;
        input.shrink(amount);
        return input;
    }

}
