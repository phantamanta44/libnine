package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.nbt.ChainingTagCompound
import xyz.phanta.libnine.util.data.nbt.deserializeVec3d
import xyz.phanta.libnine.util.data.nbt.serializeNbt
import java.util.*
import kotlin.reflect.KClass

interface DataSerializer<T : Any> {

    fun serializeNbt(tag: NBTTagCompound, name: String, value: T)

    fun deserializeNbt(tag: NBTTagCompound, name: String): T

    fun serializeByteStream(stream: ByteWriter, value: T)

    fun deserializeByteStream(stream: ByteReader): T

}

object DataSerializers {

    private val serializers: MutableMap<KClass<*>, DataSerializer<*>> = mutableMapOf()

    init {
        // primitives
        register(PrimitiveSerializer(NBTTagCompound::setInt, NBTTagCompound::getInt, ByteWriter::int, ByteReader::int))
        register(PrimitiveSerializer(NBTTagCompound::setFloat, NBTTagCompound::getFloat, ByteWriter::float, ByteReader::float))
        register(PrimitiveSerializer(NBTTagCompound::setDouble, NBTTagCompound::getDouble, ByteWriter::double, ByteReader::double))
        register(PrimitiveSerializer(NBTTagCompound::setByte, NBTTagCompound::getByte, ByteWriter::byte, ByteReader::byte))
        register(PrimitiveSerializer(NBTTagCompound::setShort, NBTTagCompound::getShort, ByteWriter::short, ByteReader::short))
        register(PrimitiveSerializer(NBTTagCompound::setLong, NBTTagCompound::getLong, ByteWriter::long, ByteReader::long))
        register(PrimitiveSerializer(NBTTagCompound::setBoolean, NBTTagCompound::getBoolean, ByteWriter::bool, ByteReader::bool))
        register(PrimitiveSerializer(NBTTagCompound::setString, NBTTagCompound::getString, ByteWriter::string, ByteReader::string))
        register(PrimitiveSerializer(NBTTagCompound::setTag, NBTTagCompound::getCompound, ByteWriter::tagCompound, ByteReader::tagCompound))

        // compounds
        register(CoerciveSerializer({ it.write(NBTTagCompound()) }, ItemStack::read, ByteWriter::itemStack, ByteReader::itemStack))
        register(CoerciveSerializer({
            TODO("forge doesn't have a fluid impl yet")
        }, {
            TODO("forge doesn't have a fluid impl yet")
        }, ByteWriter::fluid, ByteReader::fluid))
        register(CoerciveSerializer({
            TODO("forge doesn't have a fluid impl yet")
        }, {
            TODO("forge doesn't have a fluid impl yet")
        }, ByteWriter::fluidStack, ByteReader::fluidStack))
        register(CoerciveSerializer(NBTUtil::writeBlockPos, NBTUtil::readBlockPos, ByteWriter::blockPos, ByteReader::blockPos))
        register(CoerciveSerializer(Vec3d::serializeNbt, ::deserializeVec3d, ByteWriter::vec3d, ByteReader::vec3d))

        // special cases
        register(PrimitiveSerializer({ k, id ->
            setString(k, id.toString())
        }, {
            UUID.fromString(getString(it))
        }, ByteWriter::uuid, ByteReader::uuid))

        register(CoerciveSerializer({
            ChainingTagCompound().withStr("ns", it.namespace).withStr("path", it.path)
        }, {
            ResourceLocation(it.getString("ns"), it.getString("path"))
        }, ByteWriter::resourceLocation, ByteReader::resourceLocation))
    }

    private inline fun <reified T : Any> register(serializer: DataSerializer<T>) {
        serializers[T::class] = serializer
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getSerializer(type: KClass<T>): DataSerializer<T> =
            (serializers[type] ?: throw IllegalStateException("No serializer for type $type!")) as DataSerializer<T>

}

private class PrimitiveSerializer<T : Any>(
        private val nbtSerializer: NBTTagCompound.(String, T) -> Unit,
        private val nbtDeserializer: NBTTagCompound.(String) -> T,
        private val byteSerializer: ByteWriter.(T) -> Any,
        private val byteDeserializer: (ByteReader).() -> T
) : DataSerializer<T> {

    override fun serializeNbt(tag: NBTTagCompound, name: String, value: T) = nbtSerializer(tag, name, value)

    override fun deserializeNbt(tag: NBTTagCompound, name: String): T = nbtDeserializer(tag, name)

    override fun serializeByteStream(stream: ByteWriter, value: T) {
        byteSerializer(stream, value)
    }

    override fun deserializeByteStream(stream: ByteReader): T = byteDeserializer(stream)

}

private class CoerciveSerializer<T : Any>(
        private val nbtSerializer: (T) -> NBTTagCompound,
        private val nbtDeserializer: (NBTTagCompound) -> T,
        private val byteSerializer: ByteWriter.(T) -> Any,
        private val byteDeserializer: (ByteReader).() -> T
) : DataSerializer<T> {

    override fun serializeNbt(tag: NBTTagCompound, name: String, value: T) = tag.setTag(name, nbtSerializer(value))

    override fun deserializeNbt(tag: NBTTagCompound, name: String): T = nbtDeserializer(tag.getCompound(name))

    override fun serializeByteStream(stream: ByteWriter, value: T) {
        byteSerializer(stream, value)
    }

    override fun deserializeByteStream(stream: ByteReader): T = byteDeserializer(stream)

}
