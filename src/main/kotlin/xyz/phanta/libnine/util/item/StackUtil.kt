package xyz.phanta.libnine.util.item

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import xyz.phanta.libnine.util.function.DisplayableMatcher

fun ItemStack.copy(newCount: Int): ItemStack = when {
    newCount == 0 -> ItemStack.EMPTY
    newCount < 0 -> throw IllegalArgumentException("Negative stack size!")
    else -> ItemHandlerHelper.copyStackWithSize(this, newCount)
}

fun ItemStack.copyOffset(offset: Int): ItemStack = copy(this.count + offset)

infix fun ItemStack.matches(other: ItemStack): Boolean = ItemHandlerHelper.canItemStacksStack(this, other)

fun ItemStack.isCongruentWith(other: ItemStack): Boolean = ItemStack.areItemStacksEqual(this, other)

val ItemStack.countRemaining: Int
    get() = this.maxStackSize - this.count

fun ItemStack.matcher(): DisplayableMatcher<ItemStack> = DisplayableMatcher.of(this) { it.matches(this) }
