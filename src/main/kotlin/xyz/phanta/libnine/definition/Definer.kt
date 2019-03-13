package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.CompositeFeature
import net.minecraft.world.gen.feature.IFeatureConfig
import net.minecraft.world.gen.placement.IPlacementConfig
import org.apache.commons.lang3.mutable.MutableObject
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.block.BlockDefBuilder
import xyz.phanta.libnine.block.BlockDefContext
import xyz.phanta.libnine.client.gui.NineGuiContainer
import xyz.phanta.libnine.container.ContainerType
import xyz.phanta.libnine.container.NineContainer
import xyz.phanta.libnine.item.ItemDefBuilder
import xyz.phanta.libnine.item.ItemDefBuilderImpl
import xyz.phanta.libnine.item.ItemDefContext
import xyz.phanta.libnine.recipe.Recipe
import xyz.phanta.libnine.recipe.RecipeParser
import xyz.phanta.libnine.recipe.RecipeSet
import xyz.phanta.libnine.recipe.RecipeType
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.snakeify
import xyz.phanta.libnine.worldgen.BiomeSet
import xyz.phanta.libnine.worldgen.NineFeature
import xyz.phanta.libnine.worldgen.NineFeatureDistribution
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.jvmErasure

typealias DefBody<T> = T.() -> Unit

interface Definer {

    fun definitions(): DefBody<DefinitionDefContext>

}

class DefinitionDefContext(override val registrar: Registrar) : ItemDefContext, BlockDefContext {

    override fun <I : Item> item(dest: KMutableProperty0<in I>, factory: (Item.Properties) -> I, body: (ItemDefBuilder<I>) -> I) {
        val item = body(createItemBuilder(registrar.mod.resource(dest.name.snakeify()), factory))
        registrar.items += item
        dest.set(item)
    }

    override fun <I : Item> createItemBuilder(name: ResourceLocation, factory: (Item.Properties) -> I): ItemDefBuilder<I> =
            ItemDefBuilderImpl(name, factory)

    override fun <B : Block> block(
            dest: KMutableProperty0<in B>,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock,
            body: (BlockDefBuilder<B>) -> Pair<B, ItemBlock>
    ) {
        val (block, itemBlock) = body(createBlockBuilder(
                registrar.mod.resource(dest.name.snakeify()),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += itemBlock
        dest.set(block)
    }

    override fun <B : Block> createBlockBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> ItemBlock
    ): BlockDefBuilder<B> = BlockDefBuilder(name, properties, blockFactory, itemBlockFactory)

    fun itemGroup(dest: KMutableProperty0<ItemGroup>, icon: () -> ItemStack) {
        dest.set(object : ItemGroup(registrar.mod.prefix(dest.name.snakeify())) {
            override fun createIcon(): ItemStack = icon()
        })
    }

    fun <T : NineTile> tileEntity(dest: KMutableProperty0<() -> T>, factory: (Virtue, TileEntityType<T>) -> T) {
        val type = MutableObject<TileEntityType<T>>()
        val creator = { factory(registrar.mod, type.value) }
        type.value = TileEntityType.register(
                registrar.mod.prefix(dest.name.snakeify()),
                TileEntityType.Builder.create(creator)
        )
        registrar.tileEntities += type.value
        dest.set(creator)
        registrar.mod.markUsesTileEntities()
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : NineContainer, G : NineGuiContainer, X> container(
            dest: KMutableProperty0<in ContainerType<C, G, X>>,
            containerFactory: (X, InventoryPlayer, EntityPlayer) -> C,
            contextSerializer: (ByteWriter, X) -> Unit,
            contextDeserializer: (ByteReader) -> X,
            guiFactory: (C) -> G
    ) {
        val type = ContainerType(
                registrar.mod.resource(dest.name.snakeify()),
                dest.returnType.jvmErasure.java as Class<C>,
                containerFactory,
                contextSerializer,
                contextDeserializer,
                guiFactory
        )
        registrar.mod.containerHandler.register(type)
        dest.set(type)
        registrar.mod.markUsesContainers()
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : NineContainer, G : NineGuiContainer, T : NineTile> containerTileEntity(
            dest: KMutableProperty0<in ContainerType<C, G, BlockPos>>,
            containerFactory: (T) -> C,
            guiFactory: (C) -> G
    ) = container(
            dest,
            { pos, _, player -> containerFactory(player.world.getTileEntity(pos) as T) },
            { stream, pos -> stream.blockPos(pos) },
            ByteReader::blockPos,
            guiFactory
    )

    fun soundEvent(dest: KMutableProperty0<SoundEvent>, name: String) {
        val event = SoundEvent(registrar.mod.resource(name))
        event.registryName = registrar.mod.resource(dest.name.snakeify())
        registrar.soundEvents += event
        dest.set(event)
    }

    fun <I, O, R : Recipe<I, O>> recipeType(
            dest: KMutableProperty0<in RecipeType<I, O, R>>,
            parser: RecipeParser<I, O, R>,
            serializer: (ByteWriter, R) -> Unit,
            deserializer: (ByteReader) -> R
    ) {
        val type = RecipeType(registrar.mod.resource(dest.name.snakeify()), parser, serializer, deserializer)
        RecipeSet.registerType(type, registrar)
        dest.set(type)
    }

    fun worldGenFeature(
            feature: NineFeature,
            distribution: NineFeatureDistribution,
            stage: GenerationStage.Decoration,
            target: BiomeSet
    ) {
        registrar.features += Triple(CompositeFeature(
                feature.buildFeature(),
                IFeatureConfig.NO_FEATURE_CONFIG,
                distribution.buildDistribution(),
                IPlacementConfig.NO_PLACEMENT_CONFIG
        ), stage, target)
    }

}
