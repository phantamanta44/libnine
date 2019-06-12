package xyz.phanta.libnine.capability.impl

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraftforge.common.util.Constants
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemHandlerHelper
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.AbstractIncrementalData
import xyz.phanta.libnine.util.data.daedalus.AbstractIncrementalDataListener
import xyz.phanta.libnine.util.data.daedalus.ByteStreamDeltaMarker
import xyz.phanta.libnine.util.data.nbt.asNbtList
import kotlin.math.min

open class AspectInventory(size: Int) : AbstractIncrementalData<AspectInventory.Listener>(), IItemHandlerModifiable {

    private val slots: Array<ItemStack> = Array(size) { ItemStack.EMPTY }
    private val preds: Array<((ItemStack) -> Boolean)?> = arrayOfNulls(size)
    private val deltaMarker: ByteStreamDeltaMarker = ByteStreamDeltaMarker { size }

    fun withCondition(slot: Int, pred: (ItemStack) -> Boolean): AspectInventory = also { preds[slot] = pred }

    override fun getSlots(): Int = slots.size

    override fun getStackInSlot(slot: Int): ItemStack = slots[slot]

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        slots[slot] = stack
        stack.count = min(stack.count, getSlotLimit(slot))
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty) return ItemStack.EMPTY
        preds[slot]?.let { if (!it(stack)) return stack }
        if (getStackInSlot(slot).isEmpty) {
            val toTransfer = min(stack.count, getSlotLimit(slot))
            if (!simulate) setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(stack, toTransfer))
            return if (toTransfer == stack.count) {
                ItemStack.EMPTY
            } else {
                ItemHandlerHelper.copyStackWithSize(stack, stack.count - toTransfer)
            }
        } else {
            val maxStackSize = min(getStackInSlot(slot).maxStackSize, getSlotLimit(slot))
            if (getStackInSlot(slot).count >= maxStackSize || !ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), stack)) {
                return stack
            }
            val toTransfer = min(stack.count, maxStackSize - getStackInSlot(slot).count)
            if (!simulate) getStackInSlot(slot).grow(toTransfer)
            return if (toTransfer == stack.count) {
                ItemStack.EMPTY
            } else {
                ItemHandlerHelper.copyStackWithSize(stack, stack.count - toTransfer)
            }
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (amount == 0 || getStackInSlot(slot).isEmpty) return ItemStack.EMPTY
        val toTransfer = min(amount, getStackInSlot(slot).count)
        val result = ItemHandlerHelper.copyStackWithSize(getStackInSlot(slot), toTransfer)
        if (!simulate) {
            if (getStackInSlot(slot).count == toTransfer) {
                setStackInSlot(slot, ItemStack.EMPTY)
            } else {
                getStackInSlot(slot).shrink(toTransfer)
            }
        }
        return result
    }

    override fun getSlotLimit(slot: Int): Int = 64

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean = preds[slot]?.invoke(stack) ?: true

    override fun serNbt(tag: CompoundNBT) {
        tag.put("Items", slots.asNbtList {
            CompoundNBT().also { tag ->
                if (it.isEmpty) {
                    tag.putBoolean("Empty", true)
                } else {
                    it.write(tag)
                }
            }
        })
    }

    override fun deserNbt(tag: CompoundNBT) {
        val list = tag.getList("Items", Constants.NBT.TAG_COMPOUND)
        for (i in slots.indices) {
            val itemTag = list.getCompound(i)
            if (!itemTag.isEmpty) {
                setStackInSlot(i, if (itemTag.contains("Empty")) ItemStack.EMPTY else ItemStack.read(itemTag))
            }
        }
    }

    override fun serByteStream(stream: ByteWriter) {
        deltaMarker.writeFullField(stream)
        slots.forEach { stream.itemStack(it) }
    }

    override fun deserByteStream(stream: ByteReader) {
        val field = deltaMarker.readField(stream)
        for (i in slots.indices) {
            if (field[i]) {
                setStackInSlot(i, stream.itemStack())
            }
        }
    }

    override fun createListener(): Listener = Listener()

    override fun markListenerDirty(listener: Listener) {
        listener.dirty = true
    }

    inner class Listener internal constructor() : AbstractIncrementalDataListener() {

        private val lastKnownState: Array<ItemStack> = Array(slots.size) { slots[it].copy() }

        override fun clearDirtyState() {
            for (i in lastKnownState.indices) {
                lastKnownState[i] = slots[i]
            }
            super.clearDirtyState()
        }

        override fun serDeltaNbt(tag: CompoundNBT) {
            val listTag = ListNBT()
            for (i in lastKnownState.indices) {
                val itemTag = CompoundNBT()
                if (!ItemStack.areItemStacksEqual(lastKnownState[i], slots[i])) {
                    if (slots[i].isEmpty) {
                        itemTag.putBoolean("Empty", true)
                    } else {
                        slots[i].write(itemTag)
                    }
                }
                listTag.add(itemTag)
            }
            tag.put("Items", listTag)
        }

        override fun serDeltaByteStream(stream: ByteWriter) {
            val field = deltaMarker.createField()
            val subStream = ByteWriter()
            for (i in lastKnownState.indices) {
                if (!ItemStack.areItemStacksEqual(lastKnownState[i], slots[i])) {
                    subStream.itemStack(slots[i])
                    field.set(i)
                }
            }
            field.write(stream)
            stream.bytes(subStream.toArray())
        }

    }

}
