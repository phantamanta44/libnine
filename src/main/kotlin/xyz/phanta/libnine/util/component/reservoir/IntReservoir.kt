package xyz.phanta.libnine.util.component.reservoir

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.IncrementalData
import xyz.phanta.libnine.util.data.daedalus.IncrementalSerializable
import xyz.phanta.libnine.util.math.clamp
import kotlin.math.min

interface IntReservoir : IncrementalData {

    var quantity: Int

    val capacity: Int

    val remainingCapacity: Int
        get() = capacity - quantity

    fun draw(amount: Int, commit: Boolean): Int

    fun offer(amount: Int, commit: Boolean): Int

}

class SimpleIntReservoir(override var capacity: Int, quantity: Int = 0) : IncrementalSerializable(), IntReservoir {

    override var quantity: Int = quantity.clamp(0, capacity)
        set(value) {
            field = value.clamp(0, capacity)
        }

    init {
        if (capacity < 0) throw IllegalArgumentException("Negative capacity!")
    }

    override fun draw(amount: Int, commit: Boolean): Int {
        val toTransfer = min(amount, quantity)
        if (commit) quantity -= toTransfer
        return toTransfer
    }

    override fun offer(amount: Int, commit: Boolean): Int {
        val toTransfer = min(amount, remainingCapacity)
        if (commit) quantity += toTransfer
        return toTransfer
    }

    override fun serNbt(tag: CompoundNBT) = tag.putInt("Quantity", quantity)

    override fun deserNbt(tag: CompoundNBT) {
        quantity = tag.getInt("Quantity")
    }

    override fun serByteStream(stream: ByteWriter) {
        stream.int(quantity)
    }

    override fun deserByteStream(stream: ByteReader) {
        quantity = stream.int()
    }

}

class RatedIntReservoir(private val backing: IntReservoir, private val rateInwards: Int = -1, private val rateOutwards: Int = -1)
    : IntReservoir by backing {

    override fun draw(amount: Int, commit: Boolean): Int = if (rateOutwards < 0) {
        backing.draw(amount, commit)
    } else {
        backing.draw(min(amount, rateOutwards), commit)
    }

    override fun offer(amount: Int, commit: Boolean): Int = if (rateInwards < 0) {
        backing.offer(amount, commit)
    } else {
        backing.offer(min(amount, rateInwards), commit)
    }

}
