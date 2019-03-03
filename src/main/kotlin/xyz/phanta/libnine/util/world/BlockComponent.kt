package xyz.phanta.libnine.util.world

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.Serializable
import java.util.*

class SideAllocator<E : Enum<E>>(defaultState: E, private val getFront: () -> EnumFacing) : Serializable {

    private val enumType: Class<E> = defaultState.declaringClass
    private val faces: EnumMap<BlockSide, E> =
            BlockSide.VALUES.associateTo(EnumMap(BlockSide::class.java)) { it to defaultState }

    operator fun get(face: BlockSide): E = faces[face]!!

    operator fun set(face: BlockSide, state: E) {
        faces[face] = state
    }

    fun getPredicate(state: E): (EnumFacing) -> Boolean = { faces[BlockSide.fromDirection(getFront(), it)] === state }

    override fun serNbt(tag: NBTTagCompound) = faces.forEach { side, state -> tag.setString(side.name, state.name) }

    override fun deserNbt(tag: NBTTagCompound) =
            BlockSide.VALUES.forEach { faces[it] = java.lang.Enum.valueOf(enumType, tag.getString(it.name)) }

    override fun serByteStream(stream: ByteWriter) = BlockSide.VALUES.forEach { stream.enum(faces[it]!!) }

    override fun deserByteStream(stream: ByteReader) =
            BlockSide.VALUES.forEach { faces[it] = stream.enum(enumType) }

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
