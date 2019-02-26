package xyz.phanta.libnine.util.data.nbt

import net.minecraft.item.ItemStack
import net.minecraft.nbt.INBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.fluids.FluidStack

class ChainingTagCompound : NBTTagCompound() {

    fun withTag(key: String, tag: NBTTagCompound): ChainingTagCompound = also { super.setTag(key, tag) }

    fun withInt(key: String, value: Int): ChainingTagCompound = also { super.setInt(key, value) }

    fun withByte(key: String, value: Int): ChainingTagCompound = also { super.setByte(key, value.toByte()) }

    fun withFloat(key: String, value: Float): ChainingTagCompound = also { super.setFloat(key, value) }

    fun withDouble(key: String, value: Double): ChainingTagCompound = also { super.setDouble(key, value) }

    fun withBool(key: String, value: Boolean): ChainingTagCompound = also { super.setBoolean(key, value) }

    fun withStr(key: String, value: String): ChainingTagCompound = also { super.setString(key, value) }

    fun <T : INBTBase> withList(key: String, vararg tags: T): NBTTagCompound = also {
        super.setTag(key, NBTTagList().also { list ->
            tags.forEach { list.add(it) }
        })
    }

    fun withItemStack(key: String, value: ItemStack): ChainingTagCompound = withSerializable(key) { value.write(it) }

    fun withFluidStack(key: String, value: FluidStack): ChainingTagCompound = TODO("forge doesn't have a fluid impl yet")

    fun withSerializable(key: String, writer: (NBTTagCompound) -> Unit): ChainingTagCompound = also {
        super.setTag(key, NBTTagCompound().also { writer(it) })
    }

}
