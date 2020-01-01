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

    fun <T : IncrementalData> property(name: String, property: T, needsSync: Boolean): T

}

open class DataManagerImpl internal constructor() : DataManager {

    internal val properties: NavigableMap<String, PropertyEntry> = TreeMap()
    private val syncedProperties: MutableList<PropertyEntry> = mutableListOf()
    private val deltaMarker: ByteStreamDeltaMarker = ByteStreamDeltaMarker { syncedProperties.size }
    private var valid: Boolean = true

    override val dirty: Boolean
        get() = properties.any { it.value.listener.dirty }

    internal inline fun <T> checkValid(body: () -> T): T {
        check(valid) { "Daedalus instance is already shut down!" }
        return body()
    }

    override fun destroy() {
        properties.forEach { (_, v) -> v.listener.valid = false }
        properties.clear()
        valid = false
    }

    override fun genFullTag(): CompoundNBT = checkValid {
        CompoundNBT().also {
            properties.forEach { (k, v) -> it.put(k, CompoundNBT().also(v.property::serNbt)) }
        }
    }

    override fun genDeltaTag(): CompoundNBT = checkValid {
        CompoundNBT().also {
            properties.filter { (_, v) -> v.listener.dirty }.forEach { (k, v) ->
                it.put(k, CompoundNBT().also(v.listener::serDeltaNbt))
                v.listener.clearDirtyState()
            }
        }
    }

    override fun readTag(tag: CompoundNBT) = checkValid {
        tag.keySet().forEach {
            (properties[it]
                    ?: throw IllegalArgumentException("Unknown property: $it")).property.deserNbt(tag.getCompound(it))
        }
    }

    override fun genFullByteStream(stream: ByteWriter) {
        checkValid {
            deltaMarker.writeFullField(stream)
            syncedProperties.forEach { e -> e.property.serByteStream(stream) }
        }
    }

    override fun genDeltaByteStream(stream: ByteWriter) {
        checkValid {
            val field = deltaMarker.createField()
            val subStream = ByteWriter()
            syncedProperties.withIndex()
                    .filter { (_, value) -> value.listener.dirty }
                    .forEach { (index, entry) ->
                        field.set(index)
                        entry.listener.serDeltaByteStream(subStream)
                    }
            field.write(stream)
            stream.bytes(subStream.toArray())
        }
    }

    override fun readByteStream(stream: ByteReader) {
        checkValid {
            val field = deltaMarker.readField(stream)
            syncedProperties.withIndex()
                    .filter { (index, _) -> field[index] }
                    .forEach { (_, entry) ->
                        entry.property.deserByteStream(stream)
                    }
        }
    }

    override fun <T : IncrementalData> property(name: String, property: T, needsSync: Boolean): T = checkValid {
        property.also {
            PropertyEntry(it, needsSync).let { entry ->
                properties[name] = entry
                if (needsSync) {
                    syncedProperties += entry
                }
            }
        }
    }

    internal class PropertyEntry(val property: IncrementalData, val needsSync: Boolean) {
        val listener: IncrementalDataListener = property.extractListener()
    }

}

class Daedalus : DataManagerImpl() {

    private val children: MutableList<DataManager> = mutableListOf()

    fun copy(): DataManager = checkValid {
        Daedalus().also {
            properties.forEach { (k, v) -> it.property(k, v.property, v.needsSync) }
            children += it
        }
    }

    override fun destroy() {
        children.forEach(DataManager::destroy)
        super.destroy()
    }

}
