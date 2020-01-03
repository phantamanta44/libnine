package xyz.phanta.libnine.util.component.multiblock.matcher

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import xyz.phanta.libnine.util.math.computeContainingCuboid3i
import xyz.phanta.libnine.util.world.plus
import java.util.*

typealias CuboidMatcher = (World, BlockPos, BlockPos) -> Boolean

class StructureMatcherCuboid(
        private val corePos: CorePosition,
        private val wallMatcher: StructureMatcher,
        private val bodyMatcher: StructureMatcher,
        private val floorMatcher: StructureMatcher? = null,
        private val ceilMatcher: StructureMatcher? = null,
        private val wallEdgeMatcher: StructureMatcher? = null,
        private val floorEdgeMatcher: StructureMatcher? = null,
        private val ceilEdgeMatcher: StructureMatcher? = null,
        private val postTest: CuboidMatcher? = null,
        private val volumeMin: Int? = null,
        private val volumeMax: Int? = null
) : StructureMatcher {

    override fun testStructure(world: World, basePos: BlockPos, components: List<Vec3i>): Boolean {
        val minMax = components.computeContainingCuboid3i()
        val min = minMax.first
        val max = minMax.second
        val minX = min.x
        val minY = min.y
        val minZ = min.z
        val maxX = max.x
        val maxY = max.y
        val maxZ = max.z
        val dx = maxX - minX + 1
        val dy = maxY - minY + 1
        val dz = maxZ - minZ + 1
        val volume = (dx - 2) * (dy - 2) * (dz - 2)
        volumeMin?.let {
            if (volume < it) {
                return false
            }
        }
        volumeMax?.let {
            if (volume > it) {
                return false
            }
        }
        if (!corePos.isCorePositionValid(min, max)) {
            return false
        }
        val floor = mutableListOf<Vec3i>()
        val walls = mutableListOf<Vec3i>()
        val ceil = mutableListOf<Vec3i>()
        val body = mutableListOf<Vec3i>()
        components.forEach { pos ->
            when {
                pos.y == minY -> floor += pos
                pos.y == maxY -> ceil += pos
                pos.x == minX || pos.x == maxX || pos.z == minZ || pos.z == maxZ -> walls += pos
                else -> body += pos
            }
        }
        val surfaceXZ = dx * dz
        if (floor.size != surfaceXZ || ceil.size != surfaceXZ || walls.size != (dy - 2) * (dx * 2 + dz * 2 - 4)) {
            return false
        }
        floorMatcher?.let {
            if (testFailsXZ(world, basePos, floor, minX, minZ, maxX, maxZ, it, floorEdgeMatcher)) {
                return false
            }
        } ?: walls.addAll(floor)
        ceilMatcher?.let {
            if (testFailsXZ(world, basePos, ceil, minX, minZ, maxX, maxZ, it, ceilEdgeMatcher)) {
                return false
            }
        } ?: walls.addAll(ceil)
        wallEdgeMatcher?.let {
            val iter = walls.iterator()
            val wallEdge: MutableList<Vec3i> = ArrayList()
            while (iter.hasNext()) {
                val pos = iter.next()
                if (pos.x == minX || pos.x == maxX) {
                    if (pos.y == minY || pos.y == maxY || pos.z == minZ || pos.z == maxZ) {
                        wallEdge.add(pos)
                        iter.remove()
                    }
                } else if ((pos.y == minY || pos.y == maxY) && (pos.z == minZ || pos.z == maxZ)) {
                    wallEdge.add(pos)
                    iter.remove()
                }
            }
            if (!it.testStructure(world, basePos, wallEdge)) {
                return true
            }
        }
        return wallMatcher.testStructure(world, basePos, walls) && bodyMatcher.testStructure(world, basePos, body)
                && postTest?.invoke(world, basePos + min, basePos + max) ?: true
    }

    enum class CorePosition {
        ANYWHERE {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return min.x == 0 || min.y == 0 || min.z == 0 || max.x == 0 || max.y == 0 || max.z == 0
            }
        },
        IN_FACE {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return if (min.x == 0 || max.x == 0) {
                    !(min.y == 0 || max.y == 0 || min.z == 0 || max.z == 0)
                } else if (min.y == 0 || max.y == 0) {
                    !(min.z == 0 || max.z == 0)
                } else {
                    min.z == 0 || max.z == 0
                }
            }
        },
        IN_WALL_OR_EDGE {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return ((min.x == 0 || max.x == 0 || min.z == 0 || max.z == 0)
                        && !(min.y == 0 || max.y == 0))
            }
        },
        IN_WALL {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return if (min.y == 0 || max.y == 0) {
                    false
                } else if (min.x == 0 || min.y == 0) {
                    !(min.z == 0 || max.z == 0)
                } else {
                    min.z == 0 || max.z == 0
                }
            }
        },
        IN_FLOOR_OR_EDGE {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return min.y == 0
            }
        },
        IN_FLOOR {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return min.y == 0 && !(min.x == 0 || max.x == 0 || min.z == 0 || max.z == 0)
            }
        },
        IN_CEIL_OR_EDGE {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return max.y == 0
            }
        },
        IN_CEIL {
            override fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean {
                return max.y == 0 && !(min.x == 0 || max.x == 0 || min.z == 0 || max.z == 0)
            }
        };

        abstract fun isCorePositionValid(min: Vec3i, max: Vec3i): Boolean
    }

}

private fun testFailsXZ(
        world: World, basePos: BlockPos, plane: MutableList<Vec3i>,
        minX: Int, minZ: Int, maxX: Int, maxZ: Int,
        matcher: StructureMatcher, edgeMatcher: StructureMatcher?
): Boolean {
    if (edgeMatcher != null) {
        val edge = mutableListOf<Vec3i>()
        plane.removeIf { it ->
            if (it.x == minX || it.x == maxX || it.z == minZ || it.z == maxZ) {
                edge += it
                true
            } else {
                false
            }
        }
        if (!edgeMatcher.testStructure(world, basePos, edge)) {
            return true
        }
    }
    return !matcher.testStructure(world, basePos, plane)
}
