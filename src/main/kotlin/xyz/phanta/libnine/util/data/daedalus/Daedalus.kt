package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import java.util.*

interface DataManager {

    val dirty: Boolean

    fun destroy()

    fun genFullTag(): CompoundNBT

    fun genDeltaTag(): CompoundNBT

    fun readTag(tag: CompoundNBT)

    fun genFullByteStream(stream: ByteWriter)

    fun genDeltaByteStream(stream: ByteWriter)

    fun readByteStream(stream: ByteReader)

    fun <T : IncrementalData> property(name: String, property: T): T

}

open class DataManagerImpl internal constructor() : DataManager {

    internal val properties: NavigableMap<String, Pair<IncrementalData, IncrementalDataListener>> = TreeMap()
    private val deltaMarker: ByteStreamDeltaMarker = ByteStreamDeltaMarker { properties.size }
    private var valid: Boolean = true

    override val dirty: Boolean
        get() = properties.any { it.value.second.dirty }

    internal inline fun <T> checkValid(body: () -> T): T {
        if (!valid) {
            throw IllegalStateException("Daedalus instance is already shut down!")
        }
        return body()
    }

    override fun destroy() {
        properties.forEach { (_, v) -> v.second.valid = false }
        properties.clear()
        valid = false
    }

    override fun genFullTag(): CompoundNBT = checkValid {
        CompoundNBT().also {
            properties.forEach { (k, v) -> it.put(k, CompoundNBT().also(v.first::serNbt)) }
        }
    }

    override fun genDeltaTag(): CompoundNBT = checkValid {
        CompoundNBT().also {
            properties.filter { (_, v) -> v.second.dirty }.forEach { (k, v) ->
                it.put(k, CompoundNBT().also(v.second::serDeltaNbt))
                v.second.clearDirtyState()
            }
        }
    }

    override fun readTag(tag: CompoundNBT) = checkValid {
        tag.keySet().forEach {
            (properties[it]
                    ?: throw IllegalArgumentException("Unknown property: $it")).first.deserNbt(tag.getCompound(it))
        }
    }

    override fun genFullByteStream(stream: ByteWriter) {
        checkValid {
            deltaMarker.writeFullField(stream)
            properties.forEach { (_, v) -> v.first.serByteStream(stream) }
        }
    }

    override fun genDeltaByteStream(stream: ByteWriter) {
        checkValid {
            val field = deltaMarker.createField()
            val subStream = ByteWriter()
            properties.values.withIndex()
                    .filter { (_, value) -> value.second.dirty }
                    .forEach { (index, value) ->
                        field.set(index)
                        value.second.serDeltaByteStream(subStream)
                    }
            field.write(stream)
            stream.bytes(subStream.toArray())
        }
    }

    override fun readByteStream(stream: ByteReader) {
        checkValid {
            val field = deltaMarker.readField(stream)
            properties.values.withIndex()
                    .filter { (index, _) -> field[index] }
                    .forEach { (_, value) ->
                        value.first.deserByteStream(stream)
                    }
        }
    }

    override fun <T : IncrementalData> property(name: String, property: T): T = checkValid {
        property.also { properties[name] = it to it.extractListener() }
    }

}

class Daedalus : DataManagerImpl() {

    private val children: MutableList<DataManager> = mutableListOf()

    fun copy(): DataManager = checkValid {
        Daedalus().also {
            properties.forEach { (k, v) -> it.property(k, v.first) }
            children += it
        }
    }

    override fun destroy() {
        children.forEach(DataManager::destroy)
        super.destroy()
    }

}
