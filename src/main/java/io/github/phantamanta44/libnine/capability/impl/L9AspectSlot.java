package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class L9AspectSlot implements IItemHandlerModifiable, ISerializable {

    private final L9AspectInventory backing;

    L9AspectSlot(L9AspectInventory backing, @Nullable Predicate<ItemStack> pred) {
        this.backing = backing;
        if (pred != null) this.backing.withPredicate(0, pred);
    }

    public L9AspectSlot(@Nullable Predicate<ItemStack> pred) {
        this(new L9AspectInventory(1), pred);
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
        return backing.getStackInSlot(slot);
    }

    public ItemStack getStackInSlot() {
        return getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        backing.setStackInSlot(slot, stack);
    }

    public void setStackInSlot(ItemStack stack) {
        setStackInSlot(0, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return backing.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return backing.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        backing.serBytes(data);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        backing.deserBytes(data);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        backing.serNBT(tag);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        backing.deserNBT(tag);
    }

    public static class Observable extends L9AspectSlot {

        public Observable(@Nullable Predicate<ItemStack> pred, BiConsumer<ItemStack, ItemStack> observer) {
            super(new L9AspectInventory.Observable(1, (s, o, n) -> observer.accept(o, n)), pred);
        }

        public Observable(BiConsumer<ItemStack, ItemStack> observer) {
            this(null, observer);
        }

    }

}
