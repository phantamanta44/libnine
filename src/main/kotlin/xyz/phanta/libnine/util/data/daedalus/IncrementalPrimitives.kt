package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter

fun DataManager.int(name: String, initial: Int = 0): IncrementalProperty<Int> =
        this.property(name, object : IncrementalProperty<Int>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putInt("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getInt("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.int(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.int()
            }
        })

fun DataManager.float(name: String, initial: Float = 0F): IncrementalProperty<Float> =
        this.property(name, object : IncrementalProperty<Float>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putFloat("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getFloat("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.float(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.float()
            }
        })

fun DataManager.double(name: String, initial: Double = 0.0): IncrementalProperty<Double> =
        this.property(name, object : IncrementalProperty<Double>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putDouble("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getDouble("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.double(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.double()
            }
        })

fun DataManager.byte(name: String, initial: Byte = 0): IncrementalProperty<Byte> =
        this.property(name, object : IncrementalProperty<Byte>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putByte("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getByte("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.byte(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.byte()
            }
        })

fun DataManager.short(name: String, initial: Short = 0): IncrementalProperty<Short> =
        this.property(name, object : IncrementalProperty<Short>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putShort("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getShort("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.short(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.short()
            }
        })

fun DataManager.long(name: String, initial: Long = 0): IncrementalProperty<Long> =
        this.property(name, object : IncrementalProperty<Long>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putLong("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getLong("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.long(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.long()
            }
        })

fun DataManager.boolean(name: String, initial: Boolean = false): IncrementalProperty<Boolean> =
        this.property(name, object : IncrementalProperty<Boolean>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putBoolean("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getBoolean("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.bool(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.bool()
            }
        })

fun DataManager.string(name: String, initial: String): IncrementalProperty<String> =
        this.property(name, object : IncrementalProperty<String>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putString("value", value)

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getString("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.string(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.string()
            }
        })

fun DataManager.tagCompound(name: String, initial: CompoundNBT): IncrementalProperty<CompoundNBT> =
        this.property(name, object : IncrementalProperty<CompoundNBT>(initial) {
            override fun serNbt(tag: CompoundNBT) {
                tag.put("value", value)
            }

            override fun deserNbt(tag: CompoundNBT) {
                value = tag.getCompound("value")
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.tagCompound(value)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = stream.tagCompound()
            }
        })

inline fun <reified T : Enum<T>> DataManager.enum(name: String, initial: T): IncrementalProperty<T> =
        this.property(name, object : IncrementalProperty<T>(initial) {
            override fun serNbt(tag: CompoundNBT) = tag.putString("value", value.name)

            override fun deserNbt(tag: CompoundNBT) {
                value = enumValueOf(tag.getString("value"))
            }

            override fun serByteStream(stream: ByteWriter) {
                stream.int(value.ordinal)
            }

            override fun deserByteStream(stream: ByteReader) {
                value = enumValues<T>()[stream.int()]
            }
        })
