package io.github.phantamanta44.libnine.recipe.input;

import io.github.phantamanta44.libnine.util.helper.ItemUtils;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ItemStackInput implements IRcpIn<ItemStack> {

    private final Predicate<ItemStack> matcher;
    private final int amount;

    public ItemStackInput(ItemStack stack) {
        this.matcher = s -> ItemUtils.matchesWithWildcard(stack, s);
        this.amount = stack.getCount();
    }

    public ItemStackInput(Predicate<ItemStack> matcher, int amount) {
        this.matcher = matcher;
        this.amount = amount;
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
