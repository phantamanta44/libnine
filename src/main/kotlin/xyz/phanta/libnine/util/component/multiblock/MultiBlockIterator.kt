package xyz.phanta.libnine.util.component.multiblock

import java.util.*

class MultiBlockIterator<T : MultiBlockUnit<T>>(core: MultiBlockCore<T>) : Iterator<MultiBlockHost<T>> {

    private val queue: Deque<MultiBlockHost<T>> = ArrayDeque()

    init {
        queue.add(core)
    }

    override fun hasNext(): Boolean = queue.isNotEmpty()

    override fun next(): MultiBlockHost<T> = queue.pop().also { host ->
        host.emittingDirs.forEach { dir -> host.getAdjacent(dir)?.let { queue.add(it) } }
    }

}
