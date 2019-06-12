package xyz.phanta.libnine.util.world

import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import xyz.phanta.libnine.Localizable

fun Direction.isVertical(): Boolean = this == Direction.UP || this == Direction.DOWN
fun Direction.isHorizontal(): Boolean = !this.isVertical()

enum class BlockSide(private val transform: (Direction) -> Direction) : Localizable {

    FRONT({ it }),
    BACK({ it.opposite }),
    UP({ Direction.UP }),
    LEFT({ it.rotateY() }),
    DOWN({ Direction.DOWN }),
    RIGHT({ it.rotateYCCW() });

    override val displayText: ITextComponent = TranslationTextComponent("libnine.misc.blockside.${name.toLowerCase()}")

    fun getDirection(front: Direction): Direction = transform(front)

    companion object {

        val VALUES: Array<BlockSide> = values()

        fun fromDirection(front: Direction, face: Direction): BlockSide = VALUES.first { it.getDirection(front) == face }

    }

}
