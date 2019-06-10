package xyz.phanta.libnine.util.data

import net.minecraft.nbt.CompoundNBT

interface NbtSerializable {

    fun serNbt(tag: CompoundNBT)

    fun deserNbt(tag: CompoundNBT)

    fun createSerializedNbt(): CompoundNBT = CompoundNBT().also { serNbt(it) }

}

interface StreamSerializable {

    fun serByteStream(stream: ByteWriter)

    fun deserByteStream(stream: ByteReader)

}

interface Serializable : NbtSerializable, StreamSerializable
