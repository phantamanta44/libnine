package xyz.phanta.libnine.util.data.daedalus

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import kotlin.reflect.KProperty

interface IncrementalData : Serializable {

    fun extractListener(): IncrementalDataListener

}

abstract class AbstractIncrementalData<T : IncrementalDataListener> : IncrementalData {

    private val listeners: MutableList<T> = mutableListOf()

    override fun extractListener(): IncrementalDataListener = createListener().also { listeners += it }

    abstract fun createListener(): T

    protected fun iterateListeners(action: (T) -> Unit) {
        val iter = listeners.iterator()
        while (iter.hasNext()) {
            val listener = iter.next()
            if (listener.valid) {
                action(listener)
            } else {
                iter.remove()
            }
        }
    }

}

abstract class UnitIncrementalData<T : IncrementalDataListener> : AbstractIncrementalData<T>() {

    protected fun markDirty() {
        iterateListeners { markListenerDirty(it) }
    }

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

}

abstract class UnitIncrementalDataListener : AbstractIncrementalDataListener() {

    override var dirty: Boolean = false

    override fun clearDirtyState() {
        dirty = false
    }

}

abstract class IncrementalSerializable : UnitIncrementalData<IncrementalSerializable.Listener>() {

    override fun createListener(): Listener = Listener()

    override fun markListenerDirty(listener: Listener) {
        listener.dirty = true
    }

    inner class Listener internal constructor() : UnitIncrementalDataListener() {

        override fun serDeltaNbt(tag: CompoundNBT) = serNbt(tag)

        override fun serDeltaByteStream(stream: ByteWriter) = serByteStream(stream)

    }

}

abstract class IncrementalComposition<T : IncrementalComposition.Listener>(private vararg val children: IncrementalData)
    : UnitIncrementalData<T>() {

    override fun markListenerDirty(listener: T) {
        listener.dirty = true
    }

    open class Listener internal constructor(protected val owner: IncrementalComposition<*>)
        : UnitIncrementalDataListener() {

        private val childListeners: List<IncrementalDataListener> = owner.children.map { it.extractListener() }

        override var dirty: Boolean
            get() = super.dirty || childListeners.any { it.dirty }
            set(value) {
                super.dirty = value
            }

        override fun serDeltaNbt(tag: CompoundNBT) {
            childListeners.forEach { it.serDeltaNbt(tag) }
        }

        override fun serDeltaByteStream(stream: ByteWriter) {
            childListeners.forEach { it.serDeltaByteStream(stream) }
        }

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
