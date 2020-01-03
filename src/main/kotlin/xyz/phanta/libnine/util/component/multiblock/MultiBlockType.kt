package xyz.phanta.libnine.util.component.multiblock

import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.util.component.multiblock.matcher.StructureMatcher
import xyz.phanta.libnine.util.world.minus

class MultiBlockType<T : MultiBlockUnit<T>>(
        val id: ResourceLocation,
        val componentType: Class<T>,
        val maxSearchDist: Int = 64
) {

    var structureMatcher: StructureMatcher? = null

    fun checkStructure(core: MultiBlockCore<T>): Boolean = structureMatcher?.let { matcher ->
        val basePos = core.unit.pos
        matcher.testStructure(core.unit.world, basePos, core.map { it.unit.pos - basePos })
    } ?: true

    fun checkType(obj: Any?): T? = if (componentType.isInstance(obj)) componentType.cast(obj) else null

    override fun equals(other: Any?): Boolean = other is MultiBlockType<*> && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id.toString()
}
