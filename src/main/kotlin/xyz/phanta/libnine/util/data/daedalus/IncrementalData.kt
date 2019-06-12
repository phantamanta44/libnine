package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import kotlin.reflect.KProperty

interface IncrementalData : Serializable {

    fun extractListener(): IncrementalDataListener

}

abstract class AbstractIncrementalData<T : IncrementalDataListener>() : IncrementalData {

    private val listeners: MutableList<T> = mutableListOf()

    protected fun markDirty() {
        val iter = listeners.iterator()
        while (iter.hasNext()) {
            val listener = iter.next()
            if (listener.valid) {
                markListenerDirty(listener)
            } else {
                iter.remove()
            }
        }
    }

    override fun extractListener(): IncrementalDataListener = createListener().also { listeners += it }

    abstract fun createListener(): T

    abstract fun markListenerDirty(listener: T)

}

interface IncrementalDataListener {

    var valid: Boolean

    val dirty: Boolean

    fun clearDirtyState()

    fun serDeltaNbt(tag: CompoundNBT)

    fun serDeltaByteStream(stream: ByteWriter)

}

abstract class AbstractIncrementalDataListener : IncrementalDataListener {

    override var valid: Boolean = true
    override var dirty: Boolean = false

    override fun clearDirtyState() {
        dirty = false
    }

}

abstract class IncrementalSerializable : AbstractIncrementalData<IncrementalSerializable.Listener>() {

    override fun createListener(): Listener = Listener()

    override fun markListenerDirty(listener: Listener) {
        listener.dirty = true
    }

    inner class Listener internal constructor() : AbstractIncrementalDataListener() {

        override fun serDeltaNbt(tag: CompoundNBT) = serNbt(tag)

        override fun serDeltaByteStream(stream: ByteWriter) = serByteStream(stream)

    }

}

abstract class IncrementalProperty<T>(internal var value: T) : IncrementalSerializable() {

    operator fun getValue(self: Any?, property: KProperty<*>): T = value

    operator fun setValue(self: Any?, property: KProperty<*>, value: T) {
        if (this.value != value) {
            this.value = value
            markDirty()
        }
    }

}
