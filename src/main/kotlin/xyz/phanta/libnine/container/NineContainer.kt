package xyz.phanta.libnine.container

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.network.PacketClientContainerInteraction
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

abstract class NineContainer : Container {

    private val hasInvPlayer: Boolean
    private var hasInvOther: Boolean = false

    constructor(windowId: Int) : super(null, windowId) {
        hasInvPlayer = false
    }

    @Suppress("LeakingThis")
    constructor(windowId: Int, ipl: PlayerInventory, height: Int = 166) : super(null, windowId) {
        hasInvPlayer = true
        initPlayerInventory(height) { slot, x, y -> super.addSlot(Slot(ipl, slot, x, y)) }
    }

    protected open fun initPlayerInventory(height: Int, addSlot: (Int, Int, Int) -> Unit) {
        val offset = height - 82
        for (row in 0..2) {
            for (col in 0..8) addSlot(col + row * 9 + 9, 8 + col * 18, offset + row * 18)
        }
        for (col in 0..8) addSlot(col, 8 + col * 18, offset + 58)
    }

    override fun canInteractWith(player: PlayerEntity): Boolean = true

    override fun transferStackInSlot(player: PlayerEntity, index: Int): ItemStack {
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
