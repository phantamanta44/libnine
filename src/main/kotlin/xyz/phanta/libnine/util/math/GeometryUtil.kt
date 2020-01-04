package xyz.phanta.libnine.util.math

import kotlin.math.hypot

interface PlanarVec {

    companion object {

        fun of(x: Int, y: Int): PlanarVec = ImmutablePlanarVec(x, y)

    }

    private data class ImmutablePlanarVec(override val x: Int, override val y: Int) : PlanarVec

    val x: Int
    val y: Int

    operator fun component1(): Int = x
    operator fun component2(): Int = y

    fun add(xAddend: Int, yAddend: Int): PlanarVec = ImmutablePlanarVec(x + xAddend, y + yAddend)

    operator fun plus(vec: PlanarVec): PlanarVec = add(vec.x, vec.y)

    operator fun minus(vec: PlanarVec): PlanarVec = add(-vec.x, -vec.y)

    fun distanceTo(vec: PlanarVec): Double = hypot((vec.x - x).toDouble(), (vec.y - y).toDouble())

    fun inRect(origin: PlanarVec, width: Int, height: Int): Boolean =
            x >= origin.x && x < origin.x + width && y >= origin.y && y < origin.y + height

    fun inConvexHull(vararg hullVertices: PlanarVec): Boolean {
        var xgt = false
        var xlt = false
        var ygt = false
        var ylt = false
        for (vertex in hullVertices) {
            if (vertex.x <= x) {
                xlt = true
            } else if (vertex.x >= x) {
                xgt = true
            }
            if (vertex.y <= y) {
                ylt = true
            } else if (vertex.y >= y) {
                ygt = true
            }
            if (xlt && xgt && ylt && ygt) return true
        }
        return false
    }

}

class MutablePlanarVec(override var x: Int, override var y: Int) : PlanarVec {

    fun assignFrom(x: Int, y: Int): PlanarVec {
        this.x = x
        this.y = y
        return this
    }

    fun assignFrom(from: PlanarVec): PlanarVec = assignFrom(from.x, from.y)

}
