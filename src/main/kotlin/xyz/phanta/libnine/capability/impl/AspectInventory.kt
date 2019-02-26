package xyz.phanta.libnine.capability.impl

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemHandlerHelper
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import xyz.phanta.libnine.util.data.nbt.asNbtList
import kotlin.math.min

open class AspectInventory(size: Int) : IItemHandlerModifiable, Serializable {

    private val slots: Array<ItemStack> = Array(size) { ItemStack.EMPTY }
    private val preds: Array<((ItemStack) -> Boolean)?> = arrayOfNulls(size)

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

    override fun serNbt(tag: NBTTagCompound) =
            tag.setTag("Items", slots.asNbtList {
                NBTTagCompound().also { tag ->
                    if (it.isEmpty) {
                        tag.setBoolean("Empty", true)
                    } else {
                        it.write(tag)
                    }
                }
            })

    override fun deserNbt(tag: NBTTagCompound) {
        val list = tag.getList("Items", Constants.NBT.TAG_COMPOUND)
        for (i in slots.indices) {
            setStackInSlot(i, list.getCompound(i).let { if (it.hasKey("Empty")) ItemStack.EMPTY else ItemStack.read(it) })
        }
    }

    override fun serByteStream(stream: ByteWriter) = slots.forEach { stream.itemStack(it) }

    override fun deserByteStream(stream: ByteReader) {
        for (i in slots.indices) {
            setStackInSlot(i, stream.itemStack())
        }
    }

    class Observable(size: Int, private val observer: (Int, ItemStack) -> Unit) : AspectInventory(size) {

        override fun setStackInSlot(slot: Int, stack: ItemStack) {
            super.setStackInSlot(slot, stack)
            observer(slot, getStackInSlot(slot))
        }

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            if (simulate) return super.insertItem(slot, stack, true)
            val result = super.insertItem(slot, stack, false)
            observer(slot, getStackInSlot(slot))
            return result
        }

        override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
            if (simulate) return super.extractItem(slot, amount, true)
            val result = super.extractItem(slot, amount, false)
            observer(slot, getStackInSlot(slot))
            return result
        }

    }

}
