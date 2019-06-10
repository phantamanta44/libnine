package xyz.phanta.libnine.util.data.nbt

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.math.Vec3d

fun Vec3d.serializeNbt(): ChainingTagCompound =
        ChainingTagCompound().withDouble("x", this.x).withDouble("y", this.y).withDouble("z", this.z)

fun deserializeVec3d(tag: CompoundNBT): Vec3d = Vec3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"))

fun <T, N : INBT> List<T>.asNbtList(transform: (T) -> N): ListNBT = ListNBT().also { it.addAll(this.map(transform)) }

fun <T, N : INBT> Array<T>.asNbtList(transform: (T) -> N): ListNBT = ListNBT().also { it.addAll(this.map(transform)) }
