package xyz.phanta.libnine.util.world

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import xyz.phanta.libnine.util.Localizable
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

enum class RedstoneBehaviour(private val condition: (World, BlockPos) -> Boolean) : Localizable {

    IGNORED({ _, _ -> true }),
    DIRECT({ world, pos -> world.isBlockPowered(pos) }),
    INVERTED({ world, pos -> !world.isBlockPowered(pos) });

    override val displayText: ITextComponent =
            TranslationTextComponent("libnine.misc.redstonebehaviour.${name.toLowerCase()}")

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
