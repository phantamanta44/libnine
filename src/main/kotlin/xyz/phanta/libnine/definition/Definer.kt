package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntityType
import org.apache.commons.lang3.mutable.MutableObject
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.block.BlockDefBuilder
import xyz.phanta.libnine.block.BlockTemplate
import xyz.phanta.libnine.item.ItemDefBuilder
import xyz.phanta.libnine.item.ItemTemplate
import xyz.phanta.libnine.tile.NineTile
import xyz.phanta.libnine.util.snakeify
import kotlin.reflect.KMutableProperty0

typealias DefBody<T> = T.() -> Unit

interface Definer {

    fun definitions(): DefBody<DefinitionDefContext>

}

class DefinitionDefContext(private val reg: Registrar) {

    fun <I : Item> itemsBy(template: ItemTemplate<I>, body: DefBody<ItemDefContext<I>>) = body(ItemDefContext(template, reg))

    fun <B : Block> blocksBy(template: BlockTemplate<B>, body: DefBody<BlockDefContext<B>>) = body(BlockDefContext(template, reg))

    fun <T : NineTile> tileEntity(name: String, factory: (Virtue, TileEntityType<*>) -> T) {
        val type = MutableObject<TileEntityType<*>>()
        type.value = TileEntityType.register(name, TileEntityType.Builder.create { factory(reg.mod, type.value) })
        reg.tileEntities += type.value
    }

}

class ItemDefContext<I : Item>(private val template: ItemTemplate<I>, private val reg: Registrar) {

    fun item(dest: KMutableProperty0<in I>, body: (ItemDefBuilder<I>) -> Item) {
        reg.items += body(template.newBuilder(reg, dest.name.snakeify()))
    }

}

class BlockDefContext<B : Block>(private val template: BlockTemplate<B>, private val reg: Registrar) {

    fun block(dest: KMutableProperty0<in B>, body: (BlockDefBuilder<B>) -> Block) {
        reg.blocks += body(template.newBuilder(reg, dest.name.snakeify()))
    }

}
