package io.github.phantamanta44.libnine.gui;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.network.PacketClientContainerInteraction;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class L9Container extends Container {

    private final boolean hasInvPlayer;
    private boolean hasInvOther;

    public L9Container(InventoryPlayer ipl) {
        this.hasInvPlayer = true;
        this.hasInvOther = false;
        initPlayerInventory(ipl);
    }

    public L9Container() {
        this.hasInvPlayer = false;
        this.hasInvOther = false;
    }

    protected void initPlayerInventory(InventoryPlayer ipl) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                super.addSlotToContainer(new Slot(ipl, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            super.addSlotToContainer(new Slot(ipl, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (hasInvPlayer) {
            Slot slot = inventorySlots.get(index);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                ItemStack orig = stack.copy();
                boolean shouldDoPlayerInvTransfer = true;
                if (hasInvOther) {
                    if (index >= 36) {
                        if (!mergeItemStack(stack, 0, 36, false)) return ItemStack.EMPTY;
                        shouldDoPlayerInvTransfer = false;
                    } else {
                        for (int i = 36; i < inventorySlots.size(); i++) {
                            if (inventorySlots.get(i).isItemValid(stack)) {
                                if (!mergeItemStack(stack, i, i + 1, false)) return ItemStack.EMPTY;
                                if (stack.getCount() != orig.getCount()) {
                                    shouldDoPlayerInvTransfer = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (shouldDoPlayerInvTransfer) {
                    if (index < 27) {
                        if (!mergeItemStack(stack, 27, 36, false)) return ItemStack.EMPTY;
                    } else if (!mergeItemStack(stack, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (stack.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
                if (stack.getCount() != orig.getCount()) {
                    slot.onTake(player, stack);
                    return orig;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected Slot addSlotToContainer(Slot slotIn) {
        hasInvOther = true;
        return super.addSlotToContainer(slotIn);
    }

    protected void sendInteraction(byte[] data) {
        LibNine.PROXY.getRegistrar().lookUpContainerVirtue(getClass()).getNetworkHandler()
                .sendToServer(new PacketClientContainerInteraction(data));
    }

    public void onClientInteraction(ByteUtils.Reader data) {
        throw new UnsupportedOperationException("This container supports no custom client interaction!");
    }

}
