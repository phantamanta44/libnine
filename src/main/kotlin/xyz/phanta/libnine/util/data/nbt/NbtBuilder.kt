package xyz.phanta.libnine.util.data.nbt

import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fluids.FluidStack

typealias CompoundNbtDef = CompoundNBT.() -> Unit
typealias ListNbtDef = ListNBT.() -> Unit

inline fun nbtCompound(tag: CompoundNBT = CompoundNBT(), body: CompoundNbtDef): CompoundNBT = tag.also { body(it) }

inline fun nbtList(tag: ListNBT = ListNBT(), body: ListNbtDef): ListNBT = tag.also { body(it) }

fun CompoundNBT.putCompound(key: String, body: CompoundNbtDef) = put(key, nbtCompound(body = body))

fun CompoundNBT.putList(key: String, body: ListNbtDef) = put(key, nbtList(body = body))

fun CompoundNBT.putVec3d(key: String, value: Vec3d) = put(key, value.serializeNbt())

fun CompoundNBT.putBlockPos(key: String, value: BlockPos) = put(key, value.serializeNbt())

fun CompoundNBT.putResLoc(key: String, value: ResourceLocation) = put(key, value.serializeNbt())

fun CompoundNBT.putSerializable(key: String, writer: (CompoundNBT) -> Unit) = put(key, CompoundNBT().also { writer(it) })

fun CompoundNBT.putItemStack(key: String, value: ItemStack) = putSerializable(key) { value.write(it) }

fun CompoundNBT.putFluidStack(key: String, value: FluidStack) = putSerializable(key) { value.writeToNBT(it) }

fun ListNBT.addInt(value: Int) = add(IntNBT(value))

fun ListNBT.addIntArr(vararg value: Int) = add(IntArrayNBT(value))

fun ListNBT.addShort(value: Short) = add(ShortNBT(value))

fun ListNBT.addFloat(value: Float) = add(FloatNBT(value))

fun ListNBT.addDouble(value: Double) = add(DoubleNBT(value))

fun ListNBT.addStr(value: String) = add(StringNBT(value))

fun ListNBT.addCompound(body: CompoundNbtDef) = add(nbtCompound(body = body))

fun ListNBT.addList(body: ListNbtDef) = add(nbtList(body = body))

fun ListNBT.addVec3d(value: Vec3d) = add(value.serializeNbt())

fun ListNBT.addBlockPos(value: BlockPos) = add(value.serializeNbt())

fun ListNBT.addResLoc(value: ResourceLocation) = add(value.serializeNbt())

fun ListNBT.addSerializable(writer: (CompoundNBT) -> Unit) = add(CompoundNBT().also { writer(it) })

fun ListNBT.addItemStack(value: ItemStack) = addSerializable { value.write(it) }

fun ListNBT.addFluidStack(value: FluidStack) = addSerializable { value.writeToNBT(it) }
