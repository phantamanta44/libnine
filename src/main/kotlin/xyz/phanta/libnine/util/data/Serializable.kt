package xyz.phanta.libnine.util.data

import net.minecraft.nbt.NBTTagCompound

interface NbtSerializable {

    fun serNbt(tag: NBTTagCompound)

    fun deserNbt(tag: NBTTagCompound)

    fun createSerializedNbt(): NBTTagCompound = NBTTagCompound().also { serNbt(it) }

}

interface StreamSerializable {

    fun serByteStream(stream: ByteWriter)

    fun deserByteStream(stream: ByteReader)

}

interface Serializable : NbtSerializable, StreamSerializable
