package io.github.phantamanta44.libnine.gui;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.network.PacketClientContainerInteraction;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class L9Container extends Container {

    private boolean hasInvPlayer = false, hasInvOther = false;

    public L9Container(InventoryPlayer ipl, int height) {
        initPlayerInventory(ipl, height);
    }

    public L9Container(InventoryPlayer ipl) {
        this(ipl, 166);
    }

    public L9Container() {
        // NO-OP
    }

    protected void initPlayerInventory(InventoryPlayer ipl, int height) {
        int offset = height - 82;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlotToContainer(new Slot(ipl, col + row * 9 + 9, 8 + col * 18, offset + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            addSlotToContainer(new Slot(ipl, col, 8 + col * 18, offset + 58));
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
                ItemStack orig = slot.getStack();
                ItemStack stack = orig.copy();
                boolean shouldDoPlayerInvTransfer = true; // is the transfer entirely within the player's inv?
                if (hasInvOther) {
                    if (index >= 36) {
                        if (!mergeItemStack(stack, 0, 36, false)) return ItemStack.EMPTY;
                        shouldDoPlayerInvTransfer = false;
                    } else {
                        boolean changed = false;
                        for (int i = 36; i < inventorySlots.size(); i++) {
                            if (inventorySlots.get(i).isItemValid(stack)) {
                                if (mergeItemStack(stack, i, i + 1, false)) {
                                    changed = true;
                                    if (stack.isEmpty()) break;
                                }
                            }
                        }
                        if (changed) {
                            shouldDoPlayerInvTransfer = false;
                        } else {
                            return ItemStack.EMPTY;
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
                } else if (stack.getCount() != orig.getCount()) {
                    slot.putStack(stack);
                    return orig;
                } else {
                    slot.onSlotChanged();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        if (slot.inventory instanceof InventoryPlayer) {
            hasInvPlayer = true;
        } else {
            hasInvOther = true;
        }
        return super.addSlotToContainer(slot);
    }

    protected void sendInteraction(byte[] data) {
        LibNine.PROXY.getRegistrar().lookUpContainerVirtue(getClass()).getNetworkHandler()
                .sendToServer(new PacketClientContainerInteraction(data));
    }

    public void onClientInteraction(ByteUtils.Reader data) {
        throw new UnsupportedOperationException("This container supports no custom client interaction!");
    }

}
