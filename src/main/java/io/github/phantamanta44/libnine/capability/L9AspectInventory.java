package io.github.phantamanta44.libnine.capability;

import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.helper.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class L9AspectInventory implements IItemHandler, ISerializable {

    private final ItemStack[] inv;

    public L9AspectInventory(int size) {
        this.inv = new ItemStack[size];
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

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (inv[slot] == null) {
            if (!simulate) inv[slot] = stack.copy();
            return ItemStack.EMPTY;
        } else {
            if (inv[slot].getCount() >= inv[slot].getMaxStackSize()
                    || !ItemHandlerHelper.canItemStacksStack(inv[slot], stack)) {
                return ItemStack.EMPTY;
            }
            int toTransfer = Math.min(stack.getCount(), inv[slot].getMaxStackSize() - inv[slot].getCount());
            if (!simulate) inv[slot].grow(toTransfer);
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
        if (amount == 0 || inv[slot] == null) return ItemStack.EMPTY;
        int toTransfer = Math.min(amount, inv[slot].getCount());
        if (!simulate) {
            if (inv[slot].getCount() == toTransfer) {
                inv[slot] = null;
            } else {
                inv[slot].shrink(toTransfer);
            }
        }
        return ItemUtils.copyStack(inv[slot].copy(), toTransfer);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inv) {
            NBTTagCompound itemTag = new NBTTagCompound();
            if (stack == null) {
                itemTag.setBoolean("Empty", true);
            } else {
                stack.writeToNBT(itemTag);
            }
            list.appendTag(itemTag);
        }
        tag.setTag("Items", list);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inv.length; i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            inv[i] = itemTag.hasKey("Empty") ? null : new ItemStack(itemTag);
        }
    }

    @Override
    public void serializeBytes(ByteUtils.Writer data) {
        for (ItemStack stack : inv) {
            if (stack == null) {
                data.writeShort((short)-1);
            } else {
                data.writeItemStack(stack);
            }
        }
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        for (int i = 0; i < inv.length; i++) {
            if (data.readShort() == -1) {
                inv[i] = null;
            } else {
                inv[i] = data.backUp(1).readItemStack();
            }
        }
    }

}
