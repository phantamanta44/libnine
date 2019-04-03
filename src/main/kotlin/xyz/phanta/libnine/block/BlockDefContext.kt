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

typealias BlockPrimer = (BlockDefBuilder<*>) -> BlockDefBuilder<*>

interface BlockDefContextBase {

    val registrar: Registrar

    fun <B : Block> block(
            name: String,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    ): B

    fun <B : Block> block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    ) = dest.set(block(dest.name.snakeify(), blockFactory, propsFactory, itemBlockFactory, body))

    fun <B : Block> block(
            name: String,
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    ): B = block(name, blockFactory, { Block.Properties.create(material) }, itemBlockFactory, body)

    fun <B : Block> block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock> = { it.build() }
    ) = block(dest, blockFactory, { Block.Properties.create(material) }, itemBlockFactory, body)

    fun <B : Block> blocksAug(
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            primer: BlockPrimer = { it },
            body: DefBody<BlockDefContextAugmented<B>>
    ) = body(MappingBlockDefContextAugmented(registrar, this, blockFactory, propsFactory, itemBlockFactory, primer))

    fun <B : Block> blocksAug(
            blockFactory: (Block.Properties) -> B,
            material: Material,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock = ::ItemBlock,
            primer: BlockPrimer = { it },
            body: DefBody<BlockDefContextAugmented<B>>
    ) = blocksAug(blockFactory, { Block.Properties.create(material) }, itemBlockFactory, primer, body)

    fun <B : Block> createBlockBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B>

}

interface BlockDefContext : BlockDefContextBase {

    fun blocksBy(primer: BlockPrimer, body: DefBody<BlockDefContext>) =
            body(MappingBlockDefContext(registrar, this, primer))

}

interface BlockDefContextAugmented<A : Block> : BlockDefContextBase {

    fun block(name: String, body: (BlockDefBuilder<A>) -> Pair<A, ItemBlock> = { it.build() }): A

    fun block(dest: KMutableProperty0<in A>, body: (BlockDefBuilder<A>) -> Pair<A, ItemBlock> = { it.build() }) =
            dest.set(block(dest.name.snakeify(), body))

    fun blocksBy(primer: BlockPrimer, body: DefBody<BlockDefContextAugmented<A>>)

}

internal open class BlockDefContextBaseImpl(override val registrar: Registrar) : BlockDefContextBase {

    override fun <B : Block> block(
            name: String,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock>
    ): B {
        val (block, itemBlock) = body(createBlockBuilder(
                registrar.mod.resource(name),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += itemBlock
        return block
    }

    override fun <B : Block> createBlockBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B> = BlockDefBuilder(name, properties, blockFactory, itemBlockFactory)

}

private open class MappingBlockDefContextBase(
        registrar: Registrar,
        private val parent: BlockDefContextBase,
        private val primer: BlockPrimer
) : BlockDefContextBaseImpl(registrar) {

    @Suppress("UNCHECKED_CAST")
    override fun <B : Block> createBlockBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B> =
            primer(parent.createBlockBuilder(name, properties, blockFactory, itemBlockFactory)) as BlockDefBuilder<B>

}

private class MappingBlockDefContext(
        registrar: Registrar,
        parent: BlockDefContextBase,
        primer: BlockPrimer
) : MappingBlockDefContextBase(registrar, parent, primer), BlockDefContext

private class MappingBlockDefContextAugmented<A : Block>(
        registrar: Registrar,
        parent: BlockDefContextBase,
        private val blockFactory: (Block.Properties) -> A,
        private val propsFactory: () -> Block.Properties,
        private val itemBlockFactory: (A, Item.Properties) -> ItemBlock = ::ItemBlock,
        primer: BlockPrimer
) : MappingBlockDefContextBase(registrar, parent, primer), BlockDefContextAugmented<A> {

    override fun block(name: String, body: (BlockDefBuilder<A>) -> Pair<A, ItemBlock>): A {
        val (block, itemBlock) = body(createBlockBuilder(
                registrar.mod.resource(name),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += itemBlock
        return block
    }

    override fun blocksBy(primer: BlockPrimer, body: DefBody<BlockDefContextAugmented<A>>) =
            body(MappingBlockDefContextAugmented(registrar, this, blockFactory, propsFactory, itemBlockFactory, primer))

}
