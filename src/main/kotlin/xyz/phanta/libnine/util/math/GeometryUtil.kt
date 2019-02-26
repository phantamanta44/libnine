package xyz.phanta.libnine.util.math

class PlanarPoint(val x: Int, val y: Int) {

    fun add(xAddend: Int, yAddend: Int): PlanarPoint = PlanarPoint(x + xAddend, y + yAddend)

    operator fun plus(vec: PlanarPoint): PlanarPoint = add(vec.x, vec.y)

    operator fun minus(vec: PlanarPoint): PlanarPoint = add(-vec.x, -vec.y)

    fun distanceTo(vec: PlanarPoint): Double = Math.hypot((vec.x - x).toDouble(), (vec.y - y).toDouble())

    fun inRect(origin: PlanarPoint, width: Int, height: Int): Boolean =
            x >= origin.x && x < origin.x + width && y >= origin.y && y < origin.y + height

    fun inConvexHull(vararg hullVertices: PlanarPoint): Boolean {
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
