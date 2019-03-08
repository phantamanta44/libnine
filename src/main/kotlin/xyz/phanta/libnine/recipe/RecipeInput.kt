package xyz.phanta.libnine.recipe

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tags.Tag
import xyz.phanta.libnine.util.copyOffset
import xyz.phanta.libnine.util.matches

interface RecipeInput<I> {

    fun matches(input: I): Boolean

    fun consume(input: I): I

}

class TagInput(private val tag: Tag<Item>, private val amount: Int) : RecipeInput<ItemStack> {

    override fun matches(input: ItemStack): Boolean = input.count >= amount && tag.contains(input.item)

    override fun consume(input: ItemStack): ItemStack = input.copyOffset(-amount)

}

class ItemInput(private val item: Item, private val amount: Int) : RecipeInput<ItemStack> {

    override fun matches(input: ItemStack): Boolean = input.count >= amount && input.item == item

    override fun consume(input: ItemStack): ItemStack = input.copyOffset(-amount)

}

class ItemStackInput(private val stack: ItemStack) : RecipeInput<ItemStack> {

    override fun matches(input: ItemStack): Boolean = input.count >= stack.count && stack matches input

    override fun consume(input: ItemStack): ItemStack = input.copyOffset(-stack.count)

}
