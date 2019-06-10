package xyz.phanta.libnine.util.world

import net.minecraft.util.Direction

fun Direction.isVertical(): Boolean = this == Direction.UP || this == Direction.DOWN
fun Direction.isHorizontal(): Boolean = !this.isVertical()

enum class BlockSide(private val transform: (Direction) -> Direction) {

    FRONT({ it }),
    BACK({ it.opposite }),
    UP({ Direction.UP }),
    LEFT({ it.rotateY() }),
    DOWN({ Direction.DOWN }),
    RIGHT({ it.rotateYCCW() });

    fun getDirection(front: Direction): Direction = transform(front)

    companion object {

        val VALUES: Array<BlockSide> = values()

        fun fromDirection(front: Direction, face: Direction): BlockSide = VALUES.first { it.getDirection(front) == face }

    }

}
