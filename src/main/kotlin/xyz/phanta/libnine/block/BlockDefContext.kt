package xyz.phanta.libnine.block

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.definition.DefBody
import xyz.phanta.libnine.definition.Registrar
import xyz.phanta.libnine.util.snakeify
import kotlin.reflect.KMutableProperty0

interface BlockDefContextBase<B : Block> {

    val registrar: Registrar

    fun block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    )

    fun block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    ) = block(dest, blockFactory, { Block.Properties.create(material) }, itemBlockFactory, body)

    fun blocksAug(
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B> = { it },
            body: DefBody<BlockDefContextAugmented<B>>
    ) = body(MappingBlockDefContextAugmented(registrar, this, blockFactory, propsFactory, itemBlockFactory, primer))

    fun blocksAug(
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B> = { it },
            body: DefBody<BlockDefContextAugmented<B>>
    ) = blocksAug(blockFactory, { Block.Properties.create(material) }, itemBlockFactory, primer, body)

    fun createDefBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B>

}

interface BlockDefContext<B : Block> : BlockDefContextBase<B> {

    fun blocksBy(primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>, body: DefBody<BlockDefContext<B>>) =
            body(MappingBlockDefContext(registrar, this, primer))

}

interface BlockDefContextAugmented<B : Block> : BlockDefContext<B> {

    fun block(dest: KMutableProperty0<in B>, body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() })

    fun blocksAug(primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>, body: DefBody<BlockDefContextAugmented<B>>)

}

internal open class BlockDefContextBaseImpl<B : Block>(
        override val registrar: Registrar
) : BlockDefContextBase<B> {

    override fun block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock>
    ) {
        val (block, itemBlock) = body(createDefBuilder(
                registrar.mod.resource(dest.name.snakeify()),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += itemBlock
        dest.set(block)
    }

    override fun createDefBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B> = BlockDefBuilder(name, properties, blockFactory, itemBlockFactory)

}

private open class MappingBlockDefContextBase<B : Block>(
        registrar: Registrar,
        private val parent: BlockDefContextBase<B>,
        private val primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>
) : BlockDefContextBaseImpl<B>(registrar) {

    override fun createDefBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B> = primer(parent.createDefBuilder(name, properties, blockFactory, itemBlockFactory))

}

private class MappingBlockDefContext<B : Block>(
        registrar: Registrar,
        parent: BlockDefContextBase<B>,
        primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>
) : MappingBlockDefContextBase<B>(registrar, parent, primer), BlockDefContext<B>

private class MappingBlockDefContextAugmented<B : Block>(
        registrar: Registrar,
        parent: BlockDefContextBase<B>,
        private val blockFactory: (Block.Properties) -> B,
        private val propsFactory: () -> Block.Properties,
        private val itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
        primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>
) : MappingBlockDefContextBase<B>(registrar, parent, primer), BlockDefContextAugmented<B> {

    override fun block(dest: KMutableProperty0<in B>, body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock>) {
        val (block, itemBlock) = body(createDefBuilder(
                registrar.mod.resource(dest.name.snakeify()),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += itemBlock
        dest.set(block)
    }

    override fun blocksAug(primer: (BlockDefBuilder<B>) -> BlockDefBuilder<B>, body: DefBody<BlockDefContextAugmented<B>>) =
            body(MappingBlockDefContextAugmented(registrar, this, blockFactory, propsFactory, itemBlockFactory, primer))

}
