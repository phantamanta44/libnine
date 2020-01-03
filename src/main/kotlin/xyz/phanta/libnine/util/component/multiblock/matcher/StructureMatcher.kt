package xyz.phanta.libnine.util.component.multiblock.matcher

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

interface StructureMatcher {

    fun testStructure(world: World, basePos: BlockPos, components: List<Vec3i>): Boolean

}
