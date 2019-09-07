package xyz.phanta.libnine.util.math

import net.minecraft.util.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

object StdBasis {

    val X_POS: Vec3d = Vec3d(1.0, 0.0, 0.0)
    val X_NEG: Vec3d = Vec3d(-1.0, 0.0, 0.0)
    val Y_POS: Vec3d = Vec3d(0.0, 1.0, 0.0)
    val Y_NEG: Vec3d = Vec3d(0.0, -1.0, 0.0)
    val Z_POS: Vec3d = Vec3d(0.0, 0.0, 1.0)
    val Z_NEG: Vec3d = Vec3d(0.0, 0.0, -1.0)

}

fun vec3d(x: Number, y: Number, z: Number): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

operator fun Vec3d.plus(vec: Vec3d): Vec3d = this.add(vec)
operator fun Vec3d.minus(vec: Vec3d): Vec3d = this.subtract(vec)
operator fun Vec3d.times(scalar: Double): Vec3d = this.scale(scalar)
operator fun Double.times(vec: Vec3d): Vec3d = vec * this
operator fun Vec3d.div(scalar: Double): Vec3d = this.scale(1 / scalar)
operator fun Double.div(vec: Vec3d): Vec3d = vec / this

infix fun Vec3d.dot(vec: Vec3d): Double = this.dotProduct(vec)
infix fun Vec3d.cross(vec: Vec3d): Vec3d = this.crossProduct(vec)
infix fun Vec3d.onto(onto: Vec3d): Vec3d = onto.normalize().let { it * (this dot it) }

fun Vec3d.reflectPlanar(lineOfSymmetry: Vec3d): Vec3d = (this onto lineOfSymmetry) * 2.0 - this

fun Vec3d.castOntoPlane(dir: Vec3d, planarPoint: Vec3d, planeNormal: Vec3d): Vec3d? {
    val a = planeNormal dot dir
    if (a == 0.0) return null
    val scale = (planeNormal dot (planarPoint - this)) / a
    return if (scale > 0) this + dir * scale else null
}

// http://lolengine.net/blog/2013/09/21/picking-orthogonal-vector-combing-coconuts
fun Vec3d.findOrthogonal(): Vec3d = if (x.absoluteValue > z.absoluteValue) Vec3d(-y, x, 0.0) else Vec3d(0.0, -z, y)

fun Vec3d.findOrthonormal(): Vec3d = findOrthogonal().normalize()

fun Vec3d.findBasis2d(): Pair<Vec3d, Vec3d> = findOrthonormal().let { it to (it cross this).normalize() }

fun Vec3d.findBasis3d(): Triple<Vec3d, Vec3d, Vec3d> = findOrthonormal().let {
    Triple(it, (it cross this).normalize(), this.normalize())
}

fun Pair<Vec3d, Vec3d>.combinate(x: Double, y: Double): Vec3d = first * x + second * y

fun Triple<Vec3d, Vec3d, Vec3d>.combinate(x: Double, y: Double, z: Double): Vec3d = first * x + second * y + third * z

fun Vec3d.rotateAbout(axis: Vec3d, radians: Double): Vec3d {
    val parallelComponent = this onto axis
    return (this - parallelComponent).rotateOrthogonal(axis, radians) + parallelComponent
}

fun Vec3d.rotateOrthogonal(axis: Vec3d, radians: Double): Vec3d {
    val magn = axis.length()
    val i = axis.x / magn // normalized components of axis
    val j = axis.y / magn
    val k = axis.z / magn
    val cos = cos(radians)
    val ncs = 1 - cos
    val sin = sin(radians)
    return Vec3d(
            this.x * (ncs * i * i + cos) + this.y * (ncs * i * j - sin * k) + this.z * (ncs * i * k + sin * j),
            this.x * (ncs * i * j + sin * k) + this.y * (ncs * j * j + cos) + this.z * (ncs * j * k - sin * i),
            this.x * (ncs * i * k - sin * j) + this.y * (ncs * j * k + sin * i) + this.z * (ncs * k * k + cos)
    )
}

fun Direction.asVector(): Vec3d = when (this) {
    Direction.DOWN -> StdBasis.Y_NEG
    Direction.UP -> StdBasis.Y_POS
    Direction.NORTH -> StdBasis.Z_NEG
    Direction.SOUTH -> StdBasis.Z_POS
    Direction.WEST -> StdBasis.X_NEG
    Direction.EAST -> StdBasis.X_POS
}
