package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.function.IInventoryObserver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class L9AspectSlot implements IItemHandlerModifiable, ISerializable {

    private ItemStack stack = ItemStack.EMPTY;
    @Nullable
    private final Predicate<ItemStack> pred;

    public L9AspectSlot(@Nullable Predicate<ItemStack> pred) {
        this.pred = pred;
    }

    public L9AspectSlot() {
        this(null);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) throw new IndexOutOfBoundsException("Not in bounds of single-slot inventory: " + slot);
        return getStackInSlot();
    }

    public ItemStack getStackInSlot() {
        return stack;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot != 0) throw new IndexOutOfBoundsException("Not in bounds of single-slot inventory: " + slot);
        setStackInSlot(stack);
    }

    public void setStackInSlot(ItemStack stack) {
        this.stack = stack;
        int slotLimit = getSlotLimit();
        if (stack.getCount() > slotLimit) stack.setCount(slotLimit);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot != 0) throw new IndexOutOfBoundsException("Not in bounds of single-slot inventory: " + slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (pred != null && !pred.test(stack)) return stack;
        if (getStackInSlot().isEmpty()) {
            int toTransfer = Math.min(stack.getCount(), getSlotLimit());
            if (!simulate) setStackInSlot(ItemHandlerHelper.copyStackWithSize(stack, toTransfer));
            return toTransfer == stack.getCount() ? ItemStack.EMPTY
                    : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toTransfer);
        } else {
            int maxStackSize = Math.min(getStackInSlot().getMaxStackSize(), getSlotLimit());
            if (getStackInSlot().getCount() >= maxStackSize
                    || !ItemHandlerHelper.canItemStacksStack(getStackInSlot(), stack)) {
                return stack;
            }
            int toTransfer = Math.min(stack.getCount(), maxStackSize - getStackInSlot().getCount());
            if (!simulate) getStackInSlot().grow(toTransfer);
            return toTransfer == stack.getCount() ? ItemStack.EMPTY
                    : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - toTransfer);
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0) throw new IndexOutOfBoundsException("Not in bounds of single-slot inventory: " + slot);
        if (amount == 0 || getStackInSlot().isEmpty()) return ItemStack.EMPTY;
        int toTransfer = Math.min(amount, getStackInSlot().getCount());
        ItemStack result = ItemHandlerHelper.copyStackWithSize(getStackInSlot(), toTransfer);
        if (!simulate) {
            if (getStackInSlot().getCount() == toTransfer) {
                setStackInSlot(ItemStack.EMPTY);
            } else {
                getStackInSlot().shrink(toTransfer);
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return getSlotLimit();
    }

    public int getSlotLimit() {
        return 64;
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        if (stack.isEmpty()) {
            data.writeShort((short)-1);
        } else {
            data.writeItemStack(stack);
        }
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        if (data.readShort() == -1) {
            setStackInSlot(ItemStack.EMPTY);
        } else {
            setStackInSlot(data.backUp(Short.BYTES).readItemStack());
        }
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        NBTTagCompound itemTag = new NBTTagCompound();
        if (stack.isEmpty()) {
            itemTag.setBoolean("Empty", true);
        } else {
            stack.writeToNBT(itemTag);
        }
        tag.setTag("Item", itemTag);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        NBTTagCompound itemTag = tag.getCompoundTag("Item");
        setStackInSlot(itemTag.hasKey("Empty") ? ItemStack.EMPTY : new ItemStack(itemTag));
    }

    public static class Observable extends L9AspectSlot {

        private final IInventoryObserver observer;

        public Observable(@Nullable Predicate<ItemStack> pred, IInventoryObserver observer) {
            super(pred);
            this.observer = observer;
        }

        public Observable(IInventoryObserver observer) {
            this(null, observer);
        }

        @Override
        public void setStackInSlot(ItemStack stack) {
            ItemStack original = getStackInSlot().copy();
            super.setStackInSlot(stack);
            observer.onSlotChanged(0, original, stack);
        }

    }

}
