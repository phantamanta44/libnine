package xyz.phanta.libnine.util.world

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
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

    operator fun get(face: BlockSide): E = faces[face]!!

    operator fun set(face: BlockSide, state: E) {
        faces[face] = state
    }

    fun getPredicate(state: E): (Direction) -> Boolean = { faces[BlockSide.fromDirection(getFront(), it)] === state }

    override fun serNbt(tag: CompoundNBT) = faces.forEach { (side, state) -> tag.putString(side.name, state.name) }

    override fun deserNbt(tag: CompoundNBT) =
            tag.keySet().forEach { faces[enumValueOf<BlockSide>(it)] = java.lang.Enum.valueOf(enumType, tag.getString(it)) }

    override fun serByteStream(stream: ByteWriter) {
        deltaMarker.writeFullField(stream)
        BlockSide.VALUES.forEach { stream.enum(faces[it]!!) }
    }

    override fun deserByteStream(stream: ByteReader) {
        val field = deltaMarker.readField(stream)
        BlockSide.VALUES.withIndex()
                .filter { (index, _) -> field[index] }
                .forEach { (_, value) -> faces[value] = stream.enum(enumType) }
    }

    override fun createListener(): Listener = Listener()

    override fun markListenerDirty(listener: Listener) {
        listener.dirty = true
    }

    inner class Listener internal constructor() : AbstractIncrementalDataListener() {

        private val lastKnownState: EnumMap<BlockSide, E> = faces.clone()

        override fun clearDirtyState() {
            faces.forEach { (k, v) -> lastKnownState[k] = v }
            super.clearDirtyState()
        }

        override fun serDeltaNbt(tag: CompoundNBT) {
            BlockSide.VALUES.forEach {
                if (faces[it] != lastKnownState[it]) {
                    tag.putString(it.name, faces[it]!!.name)
                }
            }
        }

        override fun serDeltaByteStream(stream: ByteWriter) {
            val field = deltaMarker.createField()
            val subStream = ByteWriter()
            BlockSide.VALUES.withIndex()
                    .filter { (_, value) -> faces[value] != lastKnownState[value] }
                    .forEach { (index, value) ->
                        field.set(index)
                        subStream.enum(value)
                    }
            field.write(stream)
            stream.bytes(subStream.toArray())
        }

    }

}

enum class RedstoneBehaviour(private val condition: (World, BlockPos) -> Boolean) {

    IGNORED({ _, _ -> true }),
    DIRECT({ world, pos -> world.isBlockPowered(pos) }),
    INVERTED({ world, pos -> !world.isBlockPowered(pos) });

    fun canWork(world: World, pos: BlockPos): Boolean = condition(world, pos)

    fun next(): RedstoneBehaviour = when (this) {
        IGNORED -> DIRECT
        DIRECT -> INVERTED
        INVERTED -> IGNORED
    }

    fun prev(): RedstoneBehaviour = when (this) {
        IGNORED -> INVERTED
        INVERTED -> DIRECT
        DIRECT -> IGNORED
    }

}

interface RedstoneControllable {

    var redstoneBehaviour: RedstoneBehaviour

}
