package io.github.phantamanta44.libnine.recipe.type;

import io.github.phantamanta44.libnine.recipe.IRcp;
import io.github.phantamanta44.libnine.recipe.input.ItemStackInput;
import io.github.phantamanta44.libnine.recipe.output.ItemStackOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class SmeltingRecipe implements IRcp<ItemStack, ItemStackInput, ItemStackOutput> {

    private final ItemStackInput input;
    private final ItemStackOutput output;

    public SmeltingRecipe(ItemStack input, ItemStack output) {
        this.input = new ItemStackInput(ItemHandlerHelper.copyStackWithSize(input, 1));
        this.output = new ItemStackOutput(output.copy());
    }

    @Override
    public ItemStackInput input() {
        return input;
    }

    @Override
    public ItemStackOutput mapToOutput(ItemStack input) {
        return output;
    }

}
