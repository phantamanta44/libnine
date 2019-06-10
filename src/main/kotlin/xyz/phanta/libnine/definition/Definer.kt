package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.inventory.IInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.particles.IParticleData
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.IFeatureConfig
import net.minecraft.world.gen.placement.IPlacementConfig
import net.minecraftforge.common.crafting.RecipeType
import org.apache.commons.lang3.mutable.MutableObject
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.block.BlockDefBuilder
import xyz.phanta.libnine.block.BlockDefContext
import xyz.phanta.libnine.client.fx.NineParticleType
import xyz.phanta.libnine.item.ItemDefBuilder
import xyz.phanta.libnine.item.ItemDefBuilderImpl
import xyz.phanta.libnine.item.ItemDefContext
import xyz.phanta.libnine.recipe.RecipeSchema
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.snakeify
import xyz.phanta.libnine.worldgen.BiomeSet
import xyz.phanta.libnine.worldgen.NineFeature
import xyz.phanta.libnine.worldgen.NineFeatureDistribution
import kotlin.reflect.KMutableProperty0

typealias DefBody<T> = T.() -> Unit

interface Definer {

    fun definitions(): DefBody<DefinitionDefContext>

}

class DefinitionDefContext(override val registrar: Registrar) : ItemDefContext, BlockDefContext {

    override fun <I : Item> item(name: String, factory: (Item.Properties) -> I, body: (ItemDefBuilder<I>) -> I): I {
        val item = body(createItemBuilder(registrar.mod.resource(name), factory))
        registrar.items += item
        return item
    }

    override fun <I : Item> createItemBuilder(name: ResourceLocation, factory: (Item.Properties) -> I): ItemDefBuilder<I> =
            ItemDefBuilderImpl(name, factory)

    override fun <B : Block> block(
            name: String,
            blockFactory: (Block.Properties) -> B,
            propsFactory: () -> Block.Properties,
            itemBlockFactory: (B, Item.Properties) -> BlockItem,
            body: (BlockDefBuilder<B>) -> Pair<B, BlockItem>
    ): B {
        val (block, BlockItem) = body(createBlockBuilder(
                registrar.mod.resource(name),
                propsFactory(),
                blockFactory,
                itemBlockFactory
        ))
        registrar.blocks += block
        registrar.items += BlockItem
        return block
    }

    override fun <B : Block> createBlockBuilder(
            name: ResourceLocation,
            properties: Block.Properties,
            blockFactory: (Block.Properties) -> B,
            itemBlockFactory: (B, Item.Properties) -> BlockItem
    ): BlockDefBuilder<B> = BlockDefBuilder(name, properties, blockFactory, itemBlockFactory)

    fun itemGroup(dest: KMutableProperty0<ItemGroup>, icon: () -> ItemStack) {
        dest.set(object : ItemGroup(registrar.mod.prefix(dest.name.snakeify())) {
            override fun createIcon(): ItemStack = icon()
        })
    }

    fun <T : NineTile> tileEntity(name: String, factory: (Virtue, TileEntityType<T>) -> T, vararg blocks: Block): () -> T {
        val type = MutableObject<TileEntityType<T>>()
        val creator = { factory(registrar.mod, type.value) }
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        type.value = TileEntityType.Builder.create(creator, blocks).build(null)
        type.value.registryName = registrar.mod.resource(name)
        registrar.tileEntities += type.value
        registrar.mod.markUsesTileEntities()
        return creator
    }

    fun <T : NineTile> tileEntity(dest: KMutableProperty0<() -> T>, factory: (Virtue, TileEntityType<T>) -> T) =
            dest.set(tileEntity(dest.name.snakeify(), factory))

    // TODO containers
    /*@Suppress("UNCHECKED_CAST")
    fun <C : NineContainer, G : NineGuiContainer<C>> container(
            dest: KMutableProperty0<in ContainerType<C>>,
            containerFactory: (Int, PlayerInventory) -> C,
            guiFactory: (C, ITextComponent) -> G
    ) {
        val type = ContainerType(containerFactory)
        registrar.containerTypes += type
        ScreenManager.registerFactory(type) { container, _, title -> guiFactory(container, title) }
        dest.set(type)
        registrar.mod.markUsesContainers()
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : NineContainer, G : NineGuiContainer<C>, T : NineTile> containerTileEntity(
            dest: KMutableProperty0<in ContainerType<C>>,
            containerFactory: (T) -> C,
            guiFactory: (C, ITextComponent) -> G
    ) = container(
            dest,
            { windowId, playerInv -> containerFactory(foobar) },
            guiFactory
    )*/

    fun soundEvent(name: String, soundPath: String): SoundEvent {
        val event = SoundEvent(registrar.mod.resource(soundPath))
        event.registryName = registrar.mod.resource(name)
        registrar.soundEvents += event
        return event
    }

    fun soundEvent(dest: KMutableProperty0<SoundEvent>, soundPath: String) =
            dest.set(soundEvent(dest.name.snakeify(), soundPath))

    @Suppress("UNCHECKED_CAST")
    fun <I : IInventory, R : IRecipe<I>> recipeType(
            dest: KMutableProperty0<in RecipeType<R>>,
            schema: RecipeSchema<I, R>
    ) {
        val type = RecipeType.get(registrar.mod.resource(dest.name.snakeify()), schema.recipeType)!!
        registrar.recipeSerializers += schema
        dest.set(type)
    }

    fun worldGenFeature(
            feature: NineFeature,
            distribution: NineFeatureDistribution,
            stage: GenerationStage.Decoration,
            target: BiomeSet
    ) {
        registrar.features += Triple(Biome.createDecoratedFeature(
                feature.buildFeature(),
                IFeatureConfig.NO_FEATURE_CONFIG,
                distribution.buildDistribution(),
                IPlacementConfig.NO_PLACEMENT_CONFIG
        ), stage, target)
    }

    fun <X> particleCtx(dest: KMutableProperty0<(X) -> IParticleData>, typeFactory: (ResourceLocation) -> NineParticleType<X>) {
        val type = typeFactory(registrar.mod.resource(dest.name.snakeify()))
        registrar.particles += type
        dest.set { NineParticleType.Data(type, it) }
    }

    fun particle(dest: KMutableProperty0<() -> IParticleData>, typeFactory: (ResourceLocation) -> NineParticleType<Unit>) {
        val type = typeFactory(registrar.mod.resource(dest.name.snakeify()))
        registrar.particles += type
        dest.set { NineParticleType.Data(type, Unit) }
    }

}
