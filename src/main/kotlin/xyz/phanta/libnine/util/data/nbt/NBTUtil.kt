package xyz.phanta.libnine.util.data.nbt

import net.minecraft.nbt.INBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.math.Vec3d

fun Vec3d.serializeNbt(): ChainingTagCompound =
        ChainingTagCompound().withDouble("x", this.x).withDouble("y", this.y).withDouble("z", this.z)

fun deserializeVec3d(tag: NBTTagCompound): Vec3d = Vec3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"))

fun <T, N : INBTBase> List<T>.asNbtList(transform: (T) -> N): NBTTagList = NBTTagList().also { it.addAll(this.map(transform)) }

fun <T, N : INBTBase> Array<T>.asNbtList(transform: (T) -> N): NBTTagList = NBTTagList().also { it.addAll(this.map(transform)) }
