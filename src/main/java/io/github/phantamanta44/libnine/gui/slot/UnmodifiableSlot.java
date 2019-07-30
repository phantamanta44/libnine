package io.github.phantamanta44.libnine.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UnmodifiableSlot extends SlotItemHandler {

    public UnmodifiableSlot(IItemHandler inv, int index, int posX, int posY) {
        super(inv, index, posX, posY);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        // NO-OP
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        return ItemStack.EMPTY;
    }

}
