package xyz.phanta.libnine.util.format

import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import java.util.*

interface Localizable {

    val displayText: ITextComponent

}

private val directionDisplayText: Map<Direction, ITextComponent> = EnumMap<Direction, ITextComponent>(Direction::class.java)
        .also {
            enumValues<Direction>().forEach { dir ->
                it[dir] = TranslationTextComponent("libnine.misc.direction.${dir.name.toLowerCase()}")
            }
        }

val Direction.displayText: ITextComponent
    get() = directionDisplayText.getValue(this)
