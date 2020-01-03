package xyz.phanta.libnine.util.component.multiblock

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface MultiBlockUnit<T : MultiBlockUnit<T>> {

    val world: World
    val pos: BlockPos
    val multiBlock: MultiBlockHost<T>

}
