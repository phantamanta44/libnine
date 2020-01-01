package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.nbt.deserializeVec3d
import xyz.phanta.libnine.util.data.nbt.serializeNbt
import java.util.*

fun DataManager.blockPos(name: String, initial: BlockPos = BlockPos.ZERO, needsSync: Boolean = true): IncrementalProperty<BlockPos> =
        this.property(name, object : IncrementalProperty<BlockPos>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.put("value", NBTUtil.writeBlockPos(value))
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = NBTUtil.readBlockPos(tag.getCompound("value"))
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.blockPos(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.blockPos()
            }
        }, needsSync)

fun DataManager.vec3d(name: String, initial: Vec3d = Vec3d.ZERO, needsSync: Boolean = true): IncrementalProperty<Vec3d> =
        this.property(name, object : IncrementalProperty<Vec3d>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.put("value", value.serializeNbt())
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getCompound("value").deserializeVec3d()
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.vec3d(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.vec3d()
            }
        }, needsSync)

fun DataManager.uuid(name: String, initial: UUID = UUID.randomUUID(), needsSync: Boolean = true): IncrementalProperty<UUID> =
        this.property(name, object : IncrementalProperty<UUID>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.put("value", NBTUtil.writeUniqueId(value))
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = NBTUtil.readUniqueId(tag.getCompound("valule"))
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.uuid(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.uuid()
            }
        }, needsSync)

fun DataManager.resourceLocation(name: String, initial: ResourceLocation, needsSync: Boolean = true): IncrementalProperty<ResourceLocation> =
        this.property(name, object : IncrementalProperty<ResourceLocation>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.putString("ns", value.namespace)
                tag.putString("path", value.path)
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = ResourceLocation(tag.getString("ns"), tag.getString("path"))
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.resourceLocation(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.resourceLocation()
            }
        }, needsSync)
