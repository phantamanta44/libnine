package xyz.phanta.libnine.util.math

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d

object StdBasis {

    val X_POS: Vec3d = Vec3d(1.0, 0.0, 0.0)
    val X_NEG: Vec3d = Vec3d(-1.0, 0.0, 0.0)
    val Y_POS: Vec3d = Vec3d(0.0, 1.0, 0.0)
    val Y_NEG: Vec3d = Vec3d(0.0, -1.0, 0.0)
    val Z_POS: Vec3d = Vec3d(0.0, 0.0, 1.0)
    val Z_NEG: Vec3d = Vec3d(0.0, 0.0, -1.0)

}

operator fun Vec3d.plus(vec: Vec3d): Vec3d = this.add(vec)
operator fun Vec3d.minus(vec: Vec3d): Vec3d = this.subtract(vec)
operator fun Vec3d.times(scalar: Double): Vec3d = this.scale(scalar)

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

fun EnumFacing.asVector(): Vec3d = when (this) {
    EnumFacing.DOWN -> StdBasis.Y_NEG
    EnumFacing.UP -> StdBasis.Y_POS
    EnumFacing.NORTH -> StdBasis.Z_NEG
    EnumFacing.SOUTH -> StdBasis.Z_POS
    EnumFacing.WEST -> StdBasis.X_NEG
    EnumFacing.EAST -> StdBasis.X_POS
}
