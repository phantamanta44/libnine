package xyz.phanta.libnine.util.data.nbt

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d

fun Vec3d.serializeNbt(): ChainingTagCompound =
        ChainingTagCompound().withDouble("x", this.x).withDouble("y", this.y).withDouble("z", this.z)

fun CompoundNBT.deserializeVec3d(): Vec3d = Vec3d(getDouble("x"), getDouble("y"), getDouble("z"))

fun ResourceLocation.serializeNbt(): ChainingTagCompound =
        ChainingTagCompound().withStr("ns", this.namespace).withStr("path", this.path)

fun CompoundNBT.deserializeResourceLocation() = ResourceLocation(getString("ns"), getString("path"))

fun <T, N : INBT> List<T>.asNbtList(transform: (T) -> N): ListNBT = ListNBT().also { it.addAll(this.map(transform)) }

fun <T, N : INBT> Array<T>.asNbtList(transform: (T) -> N): ListNBT = ListNBT().also { it.addAll(this.map(transform)) }
