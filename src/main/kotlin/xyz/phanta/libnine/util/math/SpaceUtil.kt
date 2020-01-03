package xyz.phanta.libnine.util.math

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

fun List<Vec3d>.computeContainingCuboid3d(): Pair<Vec3d, Vec3d> {
    check(this.isNotEmpty()) { "Cannot compute containing cuboid of no points!" }
    val iter = this.iterator()
    val first = iter.next()
    var minX = first.x
    var minY = first.y
    var minZ = first.z
    var maxX = minX
    var maxY = minY
    var maxZ = minZ
    while (iter.hasNext()) {
        val vec = iter.next()
        if (vec.x < minX) {
            minX = vec.x
        } else if (vec.x > maxX) {
            maxX = vec.x
        }
        if (vec.y < minY) {
            minY = vec.y
        } else if (vec.y > maxY) {
            maxY = vec.y
        }
        if (vec.z < minZ) {
            minZ = vec.z
        } else if (vec.z > maxZ) {
            maxZ = vec.z
        }
    }
    return Vec3d(minX, minY, minZ) to Vec3d(maxX, maxY, maxZ)
}

fun List<Vec3i>.computeContainingCuboid3i(): Pair<Vec3i, Vec3i> {
    check(this.isNotEmpty()) { "Cannot compute containing cuboid of no points!" }
    val iter = this.iterator()
    val first = iter.next()
    var minX = first.x
    var minY = first.y
    var minZ = first.z
    var maxX = minX
    var maxY = minY
    var maxZ = minZ
    while (iter.hasNext()) {
        val vec = iter.next()
        if (vec.x < minX) {
            minX = vec.x
        } else if (vec.x > maxX) {
            maxX = vec.x
        }
        if (vec.y < minY) {
            minY = vec.y
        } else if (vec.y > maxY) {
            maxY = vec.y
        }
        if (vec.z < minZ) {
            minZ = vec.z
        } else if (vec.z > maxZ) {
            maxZ = vec.z
        }
    }
    return Vec3i(minX, minY, minZ) to Vec3i(maxX, maxY, maxZ)
}
