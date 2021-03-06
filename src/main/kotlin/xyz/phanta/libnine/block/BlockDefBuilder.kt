package xyz.phanta.libnine.block

import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.item.Item
import net.minecraft.item.BlockItem
import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.item.ItemDefBuilder

class BlockDefBuilder<B : Block>(
        private val name: ResourceLocation,
        private val properties: Block.Properties,
        private val blockFactory: (Block.Properties) -> B,
        private val itemBlockFactory: (B, Item.Properties) -> BlockItem
) {

    private val itemBlockPrimers: MutableList<(ItemBlockDefBuilder<B>) -> ItemBlockDefBuilder<B>> = mutableListOf()

    fun markIntangible(): BlockDefBuilder<B> = also { properties.doesNotBlockMovement() }

    fun withSlipperiness(slipFactor: Float): BlockDefBuilder<B> = also { properties.slipperiness(slipFactor) }

    fun withSoundType(type: SoundType): BlockDefBuilder<B> = also { properties.sound(type) }

    fun withLightEmissions(luminosity: Int): BlockDefBuilder<B> = also { properties.lightValue(luminosity) }

    fun withStrength(hardness: Float, resistance: Float = hardness): BlockDefBuilder<B> = also {
        properties.hardnessAndResistance(hardness, resistance)
    }

    fun markTicks(): BlockDefBuilder<B> = also { properties.tickRandomly() }

    fun markVaryingOpacity(): BlockDefBuilder<B> = also { properties.variableOpacity() }

    fun primeItem(primer: (ItemDefBuilder<BlockItem>) -> ItemDefBuilder<BlockItem>): BlockDefBuilder<B> = also {
        itemBlockPrimers += {
            @Suppress("UNCHECKED_CAST")
            primer(it) as ItemBlockDefBuilder<B>
        }
    }

    fun build(): Pair<B, BlockItem> = blockFactory(properties).let { block ->
        block.registryName = name
        return block to itemBlockPrimers
                .fold(ItemBlockDefBuilder(block, itemBlockFactory)) { builder, primer -> primer(builder) }
                .build()
    }

}

private class ItemBlockDefBuilder<B : Block>(
        private val block: B,
        private val itemBlockFactory: (B, Item.Properties) -> BlockItem
): ItemDefBuilder<BlockItem>() {

    override fun build(): BlockItem = itemBlockFactory(block, properties).also { it.registryName = block.registryName }

}
