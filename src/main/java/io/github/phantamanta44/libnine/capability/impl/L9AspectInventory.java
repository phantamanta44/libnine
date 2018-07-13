package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Predicate;

public class L9AspectInventory implements IItemHandlerModifiable, ISerializable {

    private final ItemStack[] inv;
    private final Predicate<ItemStack>[] preds;

    @SuppressWarnings("unchecked")
    public L9AspectInventory(int size) {
        this.inv = new ItemStack[size];
        Arrays.fill(this.inv, ItemStack.EMPTY);
        this.preds = new Predicate[size];
    }

    public L9AspectInventory withPredicate(int slot, Predicate<ItemStack> pred) {
        preds[slot] = pred;
        return this;
    }

    @Override
    public int getSlots() {
        return inv.length;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv[slot];
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        inv[slot] = stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if ((preds[slot] != null && !preds[slot].test(stack))) return stack;
        if (getStackInSlot(slot).isEmpty()) {
            int toTransfer = Math.min(stack.getCount(),
                    Math.min(getStackInSlot(slot).getMaxStackSize(), getSlotLimit(slot)));
            if (!simulate) setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(stack, toTransfer));
            if (toTransfer == stack.getCount()) return ItemStack.EMPTY;
            stack.shrink(toTransfer);
            return stack;
        } else {
            int maxStackSize = Math.min(getStackInSlot(slot).getMaxStackSize(), getSlotLimit(slot));
            if (getStackInSlot(slot).getCount() >= maxStackSize
                    || !ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), stack)) {
                return stack;
            }
            int toTransfer = Math.min(stack.getCount(), maxStackSize - getStackInSlot(slot).getCount());
            if (!simulate) getStackInSlot(slot).grow(toTransfer);
            if (toTransfer == stack.getCount()) {
                return ItemStack.EMPTY;
            } else {
                stack.shrink(toTransfer);
                return stack;
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || getStackInSlot(slot).isEmpty()) return ItemStack.EMPTY;
        int toTransfer = Math.min(amount, getStackInSlot(slot).getCount());
        ItemStack result = ItemHandlerHelper.copyStackWithSize(getStackInSlot(slot), toTransfer);
        if (!simulate) {
            if (getStackInSlot(slot).getCount() == toTransfer) {
                setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                getStackInSlot(slot).shrink(toTransfer);
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inv) {
            NBTTagCompound itemTag = new NBTTagCompound();
            if (stack.isEmpty()) {
                itemTag.setBoolean("Empty", true);
            } else {
                stack.writeToNBT(itemTag);
            }
            list.appendTag(itemTag);
        }
        tag.setTag("Items", list);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inv.length; i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            setStackInSlot(i, itemTag.hasKey("Empty") ? ItemStack.EMPTY : new ItemStack(itemTag));
        }
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        for (ItemStack stack : inv) {
            if (stack.isEmpty()) {
                data.writeShort((short)-1);
            } else {
                data.writeItemStack(stack);
            }
        }
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        for (int i = 0; i < inv.length; i++) {
            if (data.readShort() == -1) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                setStackInSlot(i, data.backUp(Short.BYTES).readItemStack());
            }
        }
    }

}