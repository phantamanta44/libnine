package xyz.phanta.libnine.util.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.probability.randomvar.ConstantVar
import xyz.phanta.libnine.util.probability.randomvar.RandomVar
import kotlin.random.Random

class ProbabilisticStack(
        val item: Item,
        val countVar: RandomVar<Int> = ConstantVar(1),
        val nbt: CompoundNBT? = null
) : RandomVar<ItemStack> {

    override fun sample(rand: Random): ItemStack {
        val stack = ItemStack(item, countVar.sample(rand))
        nbt?.let { stack.tag = it }
        return stack
    }

}
