package xyz.phanta.libnine.util.component.multiblock.matcher

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import xyz.phanta.libnine.util.world.plus

class StructureMatcherBlockState(private val condition: (BlockState) -> Boolean) : StructureMatcher {

    override fun testStructure(world: World, basePos: BlockPos, components: List<Vec3i>): Boolean =
            components.all { condition(world.getBlockState(basePos + it)) }

}
