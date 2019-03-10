package xyz.phanta.libnine.item

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraftforge.common.ToolType
import xyz.phanta.libnine.definition.Registrar
import java.util.concurrent.Callable

class ItemTemplate<I : Item>(
        internal val itemFactory: (Item.Properties) -> I,
        internal val propsFactory: () -> Item.Properties = { Item.Properties() }
) {

    fun newBuilder(reg: Registrar, name: String): ItemDefBuilder<I> = ItemDefBuilderImpl(reg, this, name)

}

abstract class ItemDefBuilder<I : Item> {

    protected abstract val properties: Item.Properties

    fun withStackSize(stackSize: Int): ItemDefBuilder<I> = also { properties.maxStackSize(stackSize) }

    fun markDamageable(maxDamage: Int): ItemDefBuilder<I> = also { properties.defaultMaxDamage(maxDamage) }

    fun withContainer(container: Item): ItemDefBuilder<I> = also { properties.containerItem(container) }

    fun withGroup(group: ItemGroup): ItemDefBuilder<I> = also { properties.group(group) }

    fun withRarity(rarity: EnumRarity): ItemDefBuilder<I> = also { properties.rarity(rarity) }

    fun markUnrepairable(): ItemDefBuilder<I> = also { properties.setNoRepair() }

    fun withToolClass(toolClass: ToolType, harvestLevel: Int): ItemDefBuilder<I> = also {
        properties.addToolType(toolClass, harvestLevel)
    }

    fun withRenderer(rendererFactory: () -> TileEntityItemStackRenderer): ItemDefBuilder<I> = also {
        properties.setTEISR { Callable { rendererFactory() } }
    }

    abstract fun build(): I

}

private class ItemDefBuilderImpl<I : Item>(
        private val reg: Registrar,
        private val template: ItemTemplate<I>,
        private val name: String
) : ItemDefBuilder<I>() {

    override val properties: Item.Properties = template.propsFactory()

    override fun build(): I = template.itemFactory(properties).also { it.setRegistryName(reg.mod.prefix(name)) }

}
