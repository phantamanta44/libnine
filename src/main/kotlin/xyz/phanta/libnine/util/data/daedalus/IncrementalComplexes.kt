package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.item.ItemStack
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

fun DataManager.itemStack(name: String, initial: ItemStack = ItemStack.EMPTY): IncrementalProperty<ItemStack> =
        this.property(name, object : IncrementalProperty<ItemStack>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                value.write(tag)
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = ItemStack.read(tag)
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.itemStack(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.itemStack()
            }
        })

// TODO fluids

fun DataManager.blockPos(name: String, initial: BlockPos = BlockPos.ZERO): IncrementalProperty<BlockPos> =
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
        })

fun DataManager.vec3d(name: String, initial: Vec3d = Vec3d.ZERO): IncrementalProperty<Vec3d> =
        this.property(name, object : IncrementalProperty<Vec3d>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.put("value", value.serializeNbt())
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = deserializeVec3d(tag.getCompound("value"))
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.vec3d(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.vec3d()
            }
        })

fun DataManager.uuid(name: String, initial: UUID = UUID.randomUUID()): IncrementalProperty<UUID> =
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
        })

fun DataManager.resourceLocation(name: String, initial: ResourceLocation): IncrementalProperty<ResourceLocation> =
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
        })
