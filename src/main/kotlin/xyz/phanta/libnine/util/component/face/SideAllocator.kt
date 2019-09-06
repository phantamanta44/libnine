package xyz.phanta.libnine.util.component.face

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.AbstractIncrementalData
import xyz.phanta.libnine.util.data.daedalus.AbstractIncrementalDataListener
import xyz.phanta.libnine.util.data.daedalus.ByteStreamDeltaMarker
import java.util.*

class SideAllocator<E : Enum<E>>(defaultState: E, private val getFront: () -> Direction)
    : AbstractIncrementalData<SideAllocator<E>.Listener>() {

    private val enumType: Class<E> = defaultState.declaringClass
    private val faces: EnumMap<BlockSide, E> =
            BlockSide.VALUES.associateTo(EnumMap(BlockSide::class.java)) { it to defaultState }
    private val deltaMarker: ByteStreamDeltaMarker = ByteStreamDeltaMarker { BlockSide.VALUES.size }

    operator fun get(face: BlockSide): E = faces.getValue(face)

    operator fun set(face: BlockSide, state: E) {
        faces[face] = state
        iterateListeners { it.dirtyState.set(face.ordinal) }
    }

    fun getPredicate(state: E): (Direction) -> Boolean = { faces[BlockSide.fromDirection(getFront(), it)] === state }

    override fun serNbt(tag: CompoundNBT) = faces.forEach { (side, state) -> tag.putString(side.name, state.name) }

    override fun deserNbt(tag: CompoundNBT) =
            tag.keySet().forEach { faces[enumValueOf<BlockSide>(it)] = java.lang.Enum.valueOf(enumType, tag.getString(it)) }

    override fun serByteStream(stream: ByteWriter) {
        deltaMarker.writeFullField(stream)
        BlockSide.VALUES.forEach { stream.enum(faces.getValue(it)) }
    }

    override fun deserByteStream(stream: ByteReader) {
        val field = deltaMarker.readField(stream)
        for (i in BlockSide.VALUES.indices) {
            if (field[i]) {
                faces[BlockSide.VALUES[i]] = stream.enum(enumType)
            }
        }
    }

    override fun createListener(): Listener = Listener()

    inner class Listener internal constructor() : AbstractIncrementalDataListener() {

        internal val dirtyState: ByteStreamDeltaMarker.MarkerField = deltaMarker.createField()

        override val dirty: Boolean
            get() = dirtyState.any()

        override fun clearDirtyState() {
            dirtyState.clear()
        }

        override fun serDeltaNbt(tag: CompoundNBT) {
            for (i in BlockSide.VALUES.indices) {
                if (dirtyState[i]) {
                    val side = BlockSide.VALUES[i]
                    tag.putString(side.name, faces.getValue(side).name)
                }
            }
        }

        override fun serDeltaByteStream(stream: ByteWriter) {
            dirtyState.write(stream)
            for (i in BlockSide.VALUES.indices) {
                if (dirtyState[i]) {
                    stream.enum(faces.getValue(BlockSide.VALUES[i]))
                }
            }
        }

    }

}
