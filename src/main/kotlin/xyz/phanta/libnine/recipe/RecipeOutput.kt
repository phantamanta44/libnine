package xyz.phanta.libnine.recipe

import net.minecraft.item.ItemStack
import xyz.phanta.libnine.util.copyOffset
import xyz.phanta.libnine.util.countRemaining
import xyz.phanta.libnine.util.matches

interface RecipeOutput<O> {

    fun isAcceptable(env: O): Boolean

    fun apply(env: O): O

}

class ItemStackOutput(private val stack: ItemStack) : RecipeOutput<ItemStack> {

    override fun isAcceptable(env: ItemStack): Boolean = env.countRemaining >= stack.count && env matches stack

    override fun apply(env: ItemStack): ItemStack = env.copyOffset(stack.count)

}
