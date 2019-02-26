package xyz.phanta.libnine.block

import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import xyz.phanta.libnine.definition.Registrar

class BlockTemplate<B : Block>(
        internal val blockFactory: (Block.Properties) -> B,
        internal val propsFactory: () -> Block.Properties
) {

    constructor(blockFactory: (Block.Properties) -> B, material: Material) : this(blockFactory, { Block.Properties.create(material) })

    fun newBuilder(reg: Registrar, name: String): BlockDefBuilder<B> = BlockDefBuilder(reg, this, name)

}

class BlockDefBuilder<B : Block>(private val reg: Registrar, private val template: BlockTemplate<B>, private val name: String) {

    private val properties: Block.Properties = template.propsFactory()

    fun markIntangible(): BlockDefBuilder<B> = also { properties.doesNotBlockMovement() }

    fun withSlipperiness(slipFactor: Float): BlockDefBuilder<B> = also { properties.slipperiness(slipFactor) }

    fun withSoundType(type: SoundType): BlockDefBuilder<B> = also { properties.sound(type) }

    fun withLightEmissions(luminosity: Int): BlockDefBuilder<B> = also { properties.lightValue(luminosity) }

    fun withStrength(hardness: Float, resistance: Float = hardness): BlockDefBuilder<B> = also {
        properties.hardnessAndResistance(hardness, resistance)
    }

    fun markTicks(): BlockDefBuilder<B> = also { properties.needsRandomTick() }

    fun markVaryingOpacity(): BlockDefBuilder<B> = also { properties.variableOpacity() }

    fun build(): B = template.blockFactory(properties).also { it.setRegistryName(reg.mod.prefix(name)) }

}
