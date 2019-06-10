package xyz.phanta.libnine.util.data.nbt

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.ListNBT
import net.minecraftforge.fluids.FluidStack

class ChainingTagCompound : CompoundNBT() {

    fun withTag(key: String, tag: CompoundNBT): ChainingTagCompound = also { super.put(key, tag) }

    fun withInt(key: String, value: Int): ChainingTagCompound = also { super.putInt(key, value) }

    fun withByte(key: String, value: Int): ChainingTagCompound = also { super.putByte(key, value.toByte()) }

    fun withFloat(key: String, value: Float): ChainingTagCompound = also { super.putFloat(key, value) }

    fun withDouble(key: String, value: Double): ChainingTagCompound = also { super.putDouble(key, value) }

    fun withBool(key: String, value: Boolean): ChainingTagCompound = also { super.putBoolean(key, value) }

    fun withStr(key: String, value: String): ChainingTagCompound = also { super.putString(key, value) }

    fun <T : INBT> withList(key: String, vararg tags: T): CompoundNBT = also {
        super.put(key, ListNBT().also { list ->
            tags.forEach { list.add(it) }
        })
    }

    fun withItemStack(key: String, value: ItemStack): ChainingTagCompound = withSerializable(key) { value.write(it) }

    fun withFluidStack(key: String, value: FluidStack): ChainingTagCompound = TODO("forge doesn't have a fluid impl yet")

    fun withSerializable(key: String, writer: (CompoundNBT) -> Unit): ChainingTagCompound = also {
        super.put(key, CompoundNBT().also { writer(it) })
    }

}
