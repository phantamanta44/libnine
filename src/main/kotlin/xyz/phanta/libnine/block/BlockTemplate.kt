package xyz.phanta.libnine.block

import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import xyz.phanta.libnine.definition.Registrar
import xyz.phanta.libnine.item.ItemDefBuilder
import xyz.phanta.libnine.item.ItemTemplate

class BlockTemplate<B : Block>(
        internal val blockFactory: (Block.Properties) -> B,
        internal val propsFactory: () -> Block.Properties,
        internal val itemBlockTemplate: ItemBlockTemplate? = null
) {

    constructor(
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockTemplate: ItemBlockTemplate? = null
    ) : this(blockFactory, { Block.Properties.create(material) }, itemBlockTemplate)

    fun newBuilder(reg: Registrar, name: String): BlockDefBuilder<B> = BlockDefBuilder(reg, this, name)

}

class BlockDefBuilder<B : Block>(private val reg: Registrar, private val template: BlockTemplate<B>, private val name: String) {

    private val properties: Block.Properties = template.propsFactory()

    private var itemBlockFactory: ((ItemDefBuilder<ItemBlock>) -> ItemBlock)? = null

    fun markIntangible(): BlockDefBuilder<B> = also { properties.doesNotBlockMovement() }

    fun withSlipperiness(slipFactor: Float): BlockDefBuilder<B> = also { properties.slipperiness(slipFactor) }

    fun withSoundType(type: SoundType): BlockDefBuilder<B> = also { properties.sound(type) }

    fun withLightEmissions(luminosity: Int): BlockDefBuilder<B> = also { properties.lightValue(luminosity) }

    fun withStrength(hardness: Float, resistance: Float = hardness): BlockDefBuilder<B> = also {
        properties.hardnessAndResistance(hardness, resistance)
    }

    fun markTicks(): BlockDefBuilder<B> = also { properties.needsRandomTick() }

    fun markVaryingOpacity(): BlockDefBuilder<B> = also { properties.variableOpacity() }

    fun withItem(body: (ItemDefBuilder<ItemBlock>) -> ItemBlock): BlockDefBuilder<B> = also {
        itemBlockFactory = body
    }

    fun build(): Pair<B, ItemBlock> = template.blockFactory(properties).let { block ->
        block.setRegistryName(reg.mod.prefix(name))
        val itemBlock = (itemBlockFactory ?: { it.build() })(
                (template.itemBlockTemplate ?: ItemBlockTemplate.DEFAULT).newBuilder(block)
        )
        return block to itemBlock
    }

}

class ItemBlockTemplate(
        internal val itemFactory: (Block, Item.Properties) -> ItemBlock = ::ItemBlock,
        internal val propsFactory: () -> Item.Properties = { Item.Properties() }
) {

    companion object {
        val DEFAULT = ItemBlockTemplate()
    }

    fun newBuilder(block: Block): ItemDefBuilder<ItemBlock> = ItemBlockDefBuilder(this, block)

}

private class ItemBlockDefBuilder(
        private val template: ItemBlockTemplate,
        private val block: Block
): ItemDefBuilder<ItemBlock>() {

    override val properties = template.propsFactory()

    override fun build(): ItemBlock = template.itemFactory(block, properties).also { it.registryName = block.registryName }

}
