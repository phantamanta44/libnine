package xyz.phanta.libnine.util.world

import net.minecraft.util.EnumFacing

fun EnumFacing.isVertical(): Boolean = this == EnumFacing.UP || this == EnumFacing.DOWN
fun EnumFacing.isHorizontal(): Boolean = !this.isVertical()

enum class BlockSide(private val transform: (EnumFacing) -> EnumFacing) {

    FRONT({ it }),
    BACK({ it.opposite }),
    UP({ EnumFacing.UP }),
    LEFT({ it.rotateY() }),
    DOWN({ EnumFacing.DOWN }),
    RIGHT({ it.rotateYCCW() });

    fun getDirection(front: EnumFacing): EnumFacing = transform(front)

    companion object {

        val VALUES: Array<BlockSide> = values()

        fun fromDirection(front: EnumFacing, face: EnumFacing): BlockSide = VALUES.first { it.getDirection(front) == face }

    }

}
