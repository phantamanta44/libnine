package xyz.phanta.libnine.item

import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import xyz.phanta.libnine.definition.DefBody
import xyz.phanta.libnine.definition.Registrar
import xyz.phanta.libnine.util.snakeify
import kotlin.reflect.KMutableProperty0

interface ItemDefContextBase<I : Item> {

    val registrar: Registrar

    fun item(
            dest: KMutableProperty0<in I>,
            factory: (Item.Properties) -> I,
            body: (ItemDefBuilder<I>) -> I = { it.build() }
    )

    fun itemsAug(
            factory: (Item.Properties) -> I,
            primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I> = { it },
            body: DefBody<ItemDefContextAugmented<I>>
    ) = body(MappingItemDefContextAugmented(registrar, this, factory, primer))

    fun createDefBuilder(name: ResourceLocation, factory: (Item.Properties) -> I): ItemDefBuilder<I>

}

interface ItemDefContext<I : Item> : ItemDefContextBase<I> {

    fun itemsBy(primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>, body: DefBody<ItemDefContext<I>>) =
            body(MappingItemDefContext(registrar, this, primer))

}

interface ItemDefContextAugmented<I : Item> : ItemDefContextBase<I> {

    fun item(dest: KMutableProperty0<in I>, body: (ItemDefBuilder<I>) -> I = { it.build() })

    fun itemsBy(primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>, body: DefBody<ItemDefContextAugmented<I>>)

}

internal open class ItemDefContextBaseImpl<I : Item>(
        override val registrar: Registrar
) : ItemDefContextBase<I> {

    override fun item(dest: KMutableProperty0<in I>, factory: (Item.Properties) -> I, body: (ItemDefBuilder<I>) -> I) {
        val item = body(createDefBuilder(registrar.mod.resource(dest.name.snakeify()), factory))
        registrar.items += item
        dest.set(item)
    }

    override fun createDefBuilder(name: ResourceLocation, factory: (Item.Properties) -> I): ItemDefBuilder<I> =
            ItemDefBuilderImpl(name, factory)

}

private open class MappingItemDefContextBase<I : Item>(
        registrar: Registrar,
        private val parent: ItemDefContextBase<I>,
        private val primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>
) : ItemDefContextBaseImpl<I>(registrar) {

    override fun createDefBuilder(name: ResourceLocation, factory: (Item.Properties) -> I): ItemDefBuilder<I> =
            primer(parent.createDefBuilder(name, factory))

}

private class MappingItemDefContext<I : Item>(
        registrar: Registrar,
        parent: ItemDefContextBase<I>,
        primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>
) : MappingItemDefContextBase<I>(registrar, parent, primer), ItemDefContext<I>

private class MappingItemDefContextAugmented<I : Item>(
        registrar: Registrar,
        parent: ItemDefContextBase<I>,
        private val factory: (Item.Properties) -> I,
        primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>
) : MappingItemDefContextBase<I>(registrar, parent, primer), ItemDefContextAugmented<I> {

    override fun item(dest: KMutableProperty0<in I>, body: (ItemDefBuilder<I>) -> I) {
        val item = body(createDefBuilder(registrar.mod.resource(dest.name.snakeify()), factory))
        registrar.items += item
        dest.set(item)
    }

    override fun itemsBy(primer: (ItemDefBuilder<I>) -> ItemDefBuilder<I>, body: DefBody<ItemDefContextAugmented<I>>) =
            body(MappingItemDefContextAugmented(registrar, this, factory, primer))

}
