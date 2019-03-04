package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.Item
import net.minecraft.resources.IResource
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import org.apache.commons.lang3.mutable.MutableObject
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.block.BlockDefBuilder
import xyz.phanta.libnine.block.BlockTemplate
import xyz.phanta.libnine.client.gui.NineGuiContainer
import xyz.phanta.libnine.container.ContainerType
import xyz.phanta.libnine.container.NineContainer
import xyz.phanta.libnine.item.ItemDefBuilder
import xyz.phanta.libnine.item.ItemTemplate
import xyz.phanta.libnine.recipe.Recipe
import xyz.phanta.libnine.recipe.RecipeParser
import xyz.phanta.libnine.recipe.RecipeSet
import xyz.phanta.libnine.recipe.RecipeType
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.data.ByteReader
import xyz.phanta.libnine.util.data.ByteWriter
import xyz.phanta.libnine.util.snakeify
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.jvmErasure

typealias DefBody<T> = T.() -> Unit

interface Definer {

    fun definitions(): DefBody<DefinitionDefContext>

}

class DefinitionDefContext(private val reg: Registrar) {

    fun <I : Item> itemsBy(template: ItemTemplate<I>, body: DefBody<ItemDefContext<I>>) = body(ItemDefContext(template, reg))

    fun <B : Block> blocksBy(template: BlockTemplate<B>, body: DefBody<BlockDefContext<B>>) = body(BlockDefContext(template, reg))

    fun <T : NineTile> tileEntity(dest: KMutableProperty0<() -> T>, factory: (Virtue, TileEntityType<*>) -> T) {
        val type = MutableObject<TileEntityType<*>>()
        val creator = { factory(reg.mod, type.value) }
        type.value = TileEntityType.register(
                reg.mod.prefix(dest.name.snakeify()),
                TileEntityType.Builder.create(creator)
        )
        reg.tileEntities += type.value
        dest.set(creator)
        reg.mod.markUsesTileEntities()
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
                reg.mod.resource(dest.name.snakeify()),
                dest.returnType.jvmErasure.java as Class<C>,
                containerFactory,
                contextSerializer,
                contextDeserializer,
                guiFactory
        )
        reg.mod.containerHandler.register(type)
        dest.set(type)
        reg.mod.markUsesContainers()
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

    fun <I, O, R : Recipe<I, O>> recipeType(
            dest: KMutableProperty0<in RecipeType<I, O, R>>,
            parser: RecipeParser<I, O, R>,
            serializer: (ByteWriter, R) -> Unit,
            deserializer: (ByteReader) -> R
    ) {
        val type = RecipeType(reg.mod.resource(dest.name.snakeify()), parser, serializer, deserializer)
        RecipeSet.registerType(type, reg)
        dest.set(type)
    }

}

class ItemDefContext<I : Item>(private val template: ItemTemplate<I>, private val reg: Registrar) {

    fun item(dest: KMutableProperty0<in I>, body: (ItemDefBuilder<I>) -> I) {
        val item = body(template.newBuilder(reg, dest.name.snakeify()))
        reg.items += item
        dest.set(item)
    }

}

class BlockDefContext<B : Block>(private val template: BlockTemplate<B>, private val reg: Registrar) {

    fun block(dest: KMutableProperty0<in B>, body: (BlockDefBuilder<B>) -> B) {
        val block = body(template.newBuilder(reg, dest.name.snakeify()))
        reg.blocks += block
        dest.set(block)
    }

}
