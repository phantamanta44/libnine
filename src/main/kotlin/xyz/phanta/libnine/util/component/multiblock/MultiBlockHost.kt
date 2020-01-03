package xyz.phanta.libnine.util.component.multiblock

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import xyz.phanta.libnine.util.collection.SmallEnumSet
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.data.daedalus.IncrementalData
import xyz.phanta.libnine.util.data.daedalus.IncrementalSerializable
import xyz.phanta.libnine.util.data.nbt.deserializeBlockPos
import xyz.phanta.libnine.util.data.nbt.putBlockPos
import xyz.phanta.libnine.util.function.CallbackSet0
import xyz.phanta.libnine.util.world.plus
import java.util.*


abstract class MultiBlockHost<T : MultiBlockUnit<T>>(val unit: T, val type: MultiBlockType<T>) : IncrementalSerializable() {

    private val _emittingDirs: SmallEnumSet<Direction> = SmallEnumSet(Direction::class.java)
    val emittingDirs: Set<Direction>
        get() = _emittingDirs

    abstract var core: MultiBlockCore<T>?
    abstract val connected: Boolean

    protected val connectionCallbacks: CallbackSet0 = CallbackSet0()

    fun tryEmit(dir: Direction): ConnectionResult = getAdjacent(dir)?.let { adj ->
        when (adj.core) {
            null -> {
                _emittingDirs += dir
                adj.core = core
                markDirty()
                ConnectionResult.SUCCESS
            }
            core -> ConnectionResult.EXISTING_CONNECTION
            else -> ConnectionResult.CONFLICT
        }
    } ?: ConnectionResult.NO_ADJACENT

    fun clearEmission() {
        _emittingDirs.forEach { getAdjacent(it)?.core = null }
        _emittingDirs.clear()
        markDirty()
    }

    abstract fun disconnect()

    fun onConnectionStateChange(callback: () -> Unit) = connectionCallbacks.addCallback(callback)

    override fun serNbt(tag: CompoundNBT) {
        tag.put("child_dirs", _emittingDirs.createSerializedNbt())
    }

    override fun deserNbt(tag: CompoundNBT) {
        _emittingDirs.deserNbt(tag.getCompound("child_dirs"))
    }

    override fun serByteStream(stream: ByteWriter) {
        _emittingDirs.serByteStream(stream)
    }

    override fun deserByteStream(stream: ByteReader) {
        _emittingDirs.deserByteStream(stream)
    }

    enum class ConnectionResult {
        SUCCESS, NO_ADJACENT, EXISTING_CONNECTION, CONFLICT
    }

}

fun <T : MultiBlockUnit<T>> MultiBlockHost<T>.getAtPos(pos: BlockPos): MultiBlockHost<T>? =
        type.checkType(unit.world.getTileEntity(pos))?.multiBlock

fun <T : MultiBlockUnit<T>> MultiBlockHost<T>.getAdjacent(dir: Direction): MultiBlockHost<T>? = getAtPos(unit.pos + dir)

class MultiBlockCore<T : MultiBlockUnit<T>>(unit: T, type: MultiBlockType<T>)
    : MultiBlockHost<T>(unit, type), Iterable<MultiBlockHost<T>>, IncrementalData {

    override var core: MultiBlockCore<T>?
        get() = this
        set(_) {
            throw UnsupportedOperationException()
        }
    override var connected: Boolean = false
        private set(value) {
            field = value
            connectionCallbacks()
        }

    fun tryForm(): Boolean {
        if (!connected) {
            val searchQueue = ArrayDeque<SearchNode<T>>()
            searchQueue.offer(SearchNode(this, null, 0))
            while (!searchQueue.isEmpty()) {
                val searchNode = searchQueue.pop()
                Direction.VALUES.forEach { dir ->
                    if (searchNode.canGoInDir(dir)) {
                        @Suppress("NON_EXHAUSTIVE_WHEN")
                        when (searchNode.host.tryEmit(dir)) {
                            ConnectionResult.SUCCESS -> if (searchNode.dist < type.maxSearchDist) {
                                searchQueue.offer(SearchNode(
                                        searchNode.host.getAdjacent(dir)!!,
                                        dir.opposite,
                                        searchNode.dist + 1
                                ))
                            }
                            ConnectionResult.CONFLICT -> {
                                destroyTree()
                                return false
                            }
                        }
                    }
                }
            }
            if (!type.checkStructure(this)) {
                destroyTree()
                return false
            }
            connected = true
        }
        return true
    }

    private fun destroyTree() {
        val searchQueue = ArrayDeque<MultiBlockHost<T>>()
        searchQueue.offer(this)
        while (!searchQueue.isEmpty()) {
            val host = searchQueue.pop()
            host.emittingDirs.forEach { dir ->
                host.getAdjacent(dir)?.let { searchQueue.offer(it) }
            }
            host.clearEmission()
        }
    }

    override fun disconnect() {
        if (connected) {
            destroyTree()
            connected = false
        }
    }

    override fun iterator(): Iterator<MultiBlockHost<T>> = MultiBlockIterator(this)

    override fun serNbt(tag: CompoundNBT) {
        super.serNbt(tag)
        tag.putBoolean("formed", connected)
    }

    override fun deserNbt(tag: CompoundNBT) {
        super.deserNbt(tag)
        connected = tag.getBoolean("formed")
    }

    override fun serByteStream(stream: ByteWriter) {
        super.serByteStream(stream)
        stream.bool(connected)
    }

    override fun deserByteStream(stream: ByteReader) {
        super.deserByteStream(stream)
        connected = stream.bool()
    }

    private class SearchNode<T : MultiBlockUnit<T>>(val host: MultiBlockHost<T>, val fromDir: Direction?, val dist: Int) {
        fun canGoInDir(dir: Direction) = dir != fromDir
    }

}

class MultiBlockComponent<T : MultiBlockUnit<T>>(unit: T, type: MultiBlockType<T>)
    : MultiBlockHost<T>(unit, type), IncrementalData {

    private var deferredCorePos: BlockPos? = null
    private var _core: MultiBlockCore<T>? = null
    override var core: MultiBlockCore<T>?
        get() = _core?.let {
            deferredCorePos = null
            it
        } ?: run {
            deferredCorePos?.let {
                _core = getAtPos(it) as MultiBlockCore<T>
                deferredCorePos = null
            }
            _core
        }
        set(value) {
            _core = value
            deferredCorePos = null
            markDirty()
            connectionCallbacks()
        }
    override val connected: Boolean
        get() = core != null

    override fun disconnect() {
        core?.disconnect()
    }

    override fun serNbt(tag: CompoundNBT) {
        super.serNbt(tag)
        core?.let { tag.putBlockPos("core_pos", it.unit.pos) }
    }

    override fun deserNbt(tag: CompoundNBT) {
        super.deserNbt(tag)
        _core = null
        if (tag.contains("core_pos")) {
            deferredCorePos = tag.getCompound("core_pos").deserializeBlockPos()
        }
    }

    override fun serByteStream(stream: ByteWriter) {
        super.serByteStream(stream)
        core?.let {
            stream.bool(true).blockPos(it.unit.pos)
        } ?: stream.bool(false)
    }

    override fun deserByteStream(stream: ByteReader) {
        super.deserByteStream(stream)
        _core = null
        if (stream.bool()) {
            deferredCorePos = stream.blockPos()
        }
    }

}
