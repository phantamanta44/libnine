package xyz.phanta.libnine.util.component.redstone

import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import xyz.phanta.libnine.util.format.Localizable

enum class RedstoneBehaviour(private val condition: (World, BlockPos) -> Boolean) : Localizable {

    IGNORED({ _, _ -> true }),
    DIRECT({ world, pos -> world.isBlockPowered(pos) }),
    INVERTED({ world, pos -> !world.isBlockPowered(pos) });

    override val displayText: ITextComponent =
            TranslationTextComponent("libnine.misc.redstonebehaviour.${name.toLowerCase()}")

    fun canWork(world: World, pos: BlockPos): Boolean = condition(world, pos)

    fun next(): RedstoneBehaviour = when (this) {
        IGNORED -> DIRECT
        DIRECT -> INVERTED
        INVERTED -> IGNORED
    }

    fun prev(): RedstoneBehaviour = when (this) {
        IGNORED -> INVERTED
        INVERTED -> DIRECT
        DIRECT -> IGNORED
    }

}
