package xyz.phanta.libnine.util

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper

fun ItemStack.copy(newCount: Int): ItemStack = when {
    newCount == 0 -> ItemStack.EMPTY
    newCount < 0 -> throw IllegalArgumentException("Negative stack size!")
    else -> ItemHandlerHelper.copyStackWithSize(this, newCount)
}

fun ItemStack.copyOffset(offset: Int): ItemStack = copy(this.count + offset)

infix fun ItemStack.matches(other: ItemStack): Boolean = ItemHandlerHelper.canItemStacksStack(this, other)

val ItemStack.countRemaining: Int
    get() = this.maxStackSize - this.count
