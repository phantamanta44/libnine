package xyz.phanta.libnine.util.collection

import net.minecraft.nbt.CompoundNBT
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.IncrementalSerializable

class SmallEnumSet<E : Enum<E>>(enumType: Class<E>) : IncrementalSerializable(), MutableSet<E> {

    private val enumConsts: Array<E> = enumType.enumConstants
    private var mask: Int = 0

    override var size: Int = 0
        private set

    init {
        check(enumConsts.size <= Int.SIZE_BITS) { "Enum type $enumType is too large for SmallEnumSet!" }
    }

    override fun isEmpty(): Boolean = mask == 0

    override fun contains(element: E): Boolean = containsMask(element.bit)

    override fun containsAll(elements: Collection<E>): Boolean = containsMask(elements.elemMask)

    private fun containsMask(elemMask: Int): Boolean = mask and elemMask == elemMask

    override fun add(element: E): Boolean = addMask(element.bit)

    override fun addAll(elements: Collection<E>): Boolean = addMask(elements.elemMask)

    private fun addMask(elemMask: Int): Boolean = setMask(mask or elemMask)

    override fun remove(element: E): Boolean = removeMask(element.bit)

    override fun removeAll(elements: Collection<E>): Boolean = removeMask(elements.elemMask)

    private fun removeMask(elemMask: Int): Boolean = setMask(mask and elemMask.inv())

    override fun retainAll(elements: Collection<E>): Boolean = setMask(mask and elements.elemMask)

    override fun clear() {
        setMask(0)
    }

    private fun setMask(newMask: Int): Boolean = if (mask != newMask) {
        mask = newMask
        size = Integer.bitCount(mask)
        markDirty()
        true
    } else {
        false
    }

    override fun iterator(): MutableIterator<E> = SmallEnumSetIterator()

    private val E.bit: Int
        get() = 1 shl this.ordinal

    private val Collection<E>.elemMask: Int
        get() = this.fold(0) { acc, elem -> acc or elem.bit }

    override fun serNbt(tag: CompoundNBT) {
        tag.putInt("mask", mask)
    }

    override fun deserNbt(tag: CompoundNBT) {
        mask = tag.getInt("mask")
    }

    override fun serByteStream(stream: ByteWriter) {
        stream.int(mask)
    }

    override fun deserByteStream(stream: ByteReader) {
        mask = stream.int()
    }

    private inner class SmallEnumSetIterator : MutableIterator<E> {

        private var iterMask: Int = 0
        private var iterMaskNext: Int = 1
        private var bitIndex: Int = -1

        override fun hasNext(): Boolean = iterMaskNext != 0

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException()
            while (iterMaskNext != 0 && mask and iterMaskNext == 0) {
                advance()
            }
            iterMask = iterMaskNext
            advance()
            return enumConsts[bitIndex]
        }

        private fun advance() {
            iterMaskNext = iterMaskNext shl 1
            ++bitIndex
        }

        override fun remove() {
            setMask(mask and iterMask.inv())
        }

    }

}
