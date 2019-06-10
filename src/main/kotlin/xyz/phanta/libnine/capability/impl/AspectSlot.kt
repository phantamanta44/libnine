package xyz.phanta.libnine.capability.impl

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemHandlerHelper
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import kotlin.math.min

open class AspectSlot(private val pred: ((ItemStack) -> Boolean)? = null) : IItemHandlerModifiable, Serializable {

    protected open var stored: ItemStack = ItemStack.EMPTY
        set(stack) {
            field = stack.also { stack.count = min(stack.count, slotLimit) }
        }

    private val slotLimit: Int
        get() = 64

    override fun getSlots(): Int = 1

    override fun getStackInSlot(slot: Int): ItemStack {
        if (slot != 0) throw IndexOutOfBoundsException("Not in bounds of single-slot inventory: $slot")
        return stored
    }

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        if (slot != 0) throw IndexOutOfBoundsException("Not in bounds of single-slot inventory: $slot")
        stored = stack
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (slot != 0) throw IndexOutOfBoundsException("Not in bounds of single-slot inventory: $slot")
        return insertItem(stack, simulate)
    }

    open fun insertItem(stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty) return ItemStack.EMPTY
        pred?.let { if (!it(stack)) return stack }
        if (stored.isEmpty) {
            val toTransfer = min(stack.count, slotLimit)
            if (!simulate) stored = ItemHandlerHelper.copyStackWithSize(stack, toTransfer)
            return if (toTransfer == stack.count) {
                ItemStack.EMPTY
            } else {
                ItemHandlerHelper.copyStackWithSize(stack, stack.count - toTransfer)
            }
        } else {
            val maxStackSize = min(stored.maxStackSize, slotLimit)
            if (stored.count >= maxStackSize || !ItemHandlerHelper.canItemStacksStack(stored, stack)) {
                return stack
            }
            val toTransfer = min(stack.count, maxStackSize - stored.count)
            if (!simulate) stored.grow(toTransfer)
            return if (toTransfer == stack.count) {
                ItemStack.EMPTY
            } else {
                ItemHandlerHelper.copyStackWithSize(stack, stack.count - toTransfer)
            }
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (slot != 0) throw IndexOutOfBoundsException("Not in bounds of single-slot inventory: $slot")
        return extractItem(amount, simulate)
    }

    open fun extractItem(amount: Int, simulate: Boolean): ItemStack {
        if (amount == 0 || stored.isEmpty) return ItemStack.EMPTY
        val toTransfer = min(amount, stored.count)
        val result = ItemHandlerHelper.copyStackWithSize(stored, toTransfer)
        if (!simulate) {
            if (stored.count == toTransfer) {
                stored = ItemStack.EMPTY
            } else {
                stored.shrink(toTransfer)
            }
        }
        return result
    }

    override fun getSlotLimit(slot: Int): Int = slotLimit

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        if (slot != 0) throw IndexOutOfBoundsException("Not in bounds of single-slot inventory: $slot")
        return pred?.invoke(stack) ?: true
    }

    override fun serByteStream(stream: ByteWriter) {
        stream.itemStack(stored)
    }

    override fun deserByteStream(stream: ByteReader) {
        stored = stream.itemStack()
    }

    override fun serNbt(tag: CompoundNBT) {
        tag.put("Item", CompoundNBT().also {
            if (stored.isEmpty) {
                it.putBoolean("Empty", true)
            } else {
                stored.write(it)
            }
        })
    }

    override fun deserNbt(tag: CompoundNBT) {
        stored = tag.getCompound("Item").let { if (it.contains("Empty")) ItemStack.EMPTY else ItemStack.read(it) }
    }

    class Observable(pred: ((ItemStack) -> Boolean)?, private val observer: (Int, ItemStack) -> Unit) : AspectSlot(pred) {

        override var stored: ItemStack
            get() = super.stored
            set(stack) {
                super.stored = stack
                observer(0, stored)
            }

        constructor(observer: (Int, ItemStack) -> Unit) : this(null, observer)

        override fun insertItem(stack: ItemStack, simulate: Boolean): ItemStack {
            if (simulate) return super.insertItem(stack, true)
            val result = super.insertItem(stack, false)
            observer(0, stored)
            return result
        }

        override fun extractItem(amount: Int, simulate: Boolean): ItemStack {
            if (simulate) return super.extractItem(amount, true)
            val result = super.extractItem(amount, false)
            observer(0, stored)
            return result
        }

    }

}
