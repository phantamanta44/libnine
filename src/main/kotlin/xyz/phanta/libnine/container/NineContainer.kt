package xyz.phanta.libnine.container

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.network.PacketClientContainerInteraction
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

abstract class NineContainer : Container {

    private val hasInvPlayer: Boolean
    private var hasInvOther: Boolean = false

    constructor() {
        hasInvPlayer = false
    }

    @Suppress("LeakingThis")
    constructor(ipl: InventoryPlayer) {
        hasInvPlayer = true
        initPlayerInventory { slot, x, y -> super.addSlot(Slot(ipl, slot, x, y)) }
    }

    protected open fun initPlayerInventory(addSlot: (Int, Int, Int) -> Unit) {
        for (i in 0..2) {
            for (j in 0..8) addSlot(j + i * 9 + 9, 8 + j * 18, 84 + i * 18)
        }
        for (i in 0..8) addSlot(i, 8 + i * 18, 142)
    }

    override fun canInteractWith(player: EntityPlayer): Boolean = true

    override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack {
        if (hasInvPlayer) {
            val slot = inventorySlots[index]
            if (slot != null && slot.hasStack) {
                val stack = slot.stack
                val orig = stack.copy()
                var shouldDoPlayerInvTransfer = true // is the transfer entirely within the player's inv?
                if (hasInvOther) {
                    if (index >= 36) {
                        if (!mergeItemStack(stack, 0, 36, false)) return ItemStack.EMPTY
                        shouldDoPlayerInvTransfer = false
                    } else {
                        var changed = false
                        for (i in 36 until inventorySlots.size) {
                            if (inventorySlots[i].isItemValid(stack)) {
                                if (mergeItemStack(stack, i, i + 1, false)) {
                                    changed = true
                                    if (stack.isEmpty) break
                                }
                            }
                        }
                        if (changed) {
                            shouldDoPlayerInvTransfer = false
                        } else {
                            return ItemStack.EMPTY
                        }
                    }
                }
                if (shouldDoPlayerInvTransfer) {
                    if (index < 27) {
                        if (!mergeItemStack(stack, 27, 36, false)) return ItemStack.EMPTY
                    } else if (!mergeItemStack(stack, 0, 27, false)) {
                        return ItemStack.EMPTY
                    }
                }
                if (stack.count == 0) {
                    slot.putStack(ItemStack.EMPTY)
                } else {
                    slot.onSlotChanged()
                }
                if (stack.count != orig.count) {
                    slot.onTake(player, stack)
                    return orig
                }
            }
        }
        return ItemStack.EMPTY
    }

    override fun addSlot(slotIn: Slot): Slot {
        hasInvOther = true
        return addSlot(slotIn)
    }

    protected fun sendInteraction(body: (ByteWriter) -> Unit) {
        Virtue.forContainer(javaClass).netHandler.postToServer(
                PacketClientContainerInteraction.Packet(ByteWriter().also { body(it) }.toArray())
        )
    }

    open fun onClientInteraction(data: ByteReader) {
        throw UnsupportedOperationException("This container supports no custom client interaction!")
    }

}
