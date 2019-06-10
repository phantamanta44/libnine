package xyz.phanta.libnine.item

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.Rarity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.ToolType
import java.util.concurrent.Callable

abstract class ItemDefBuilder<I : Item> {

    protected val properties: Item.Properties = Item.Properties()

    fun withStackSize(stackSize: Int): ItemDefBuilder<I> = also { properties.maxStackSize(stackSize) }

    fun markDamageable(maxDamage: Int): ItemDefBuilder<I> = also { properties.defaultMaxDamage(maxDamage) }

    fun withContainer(container: Item): ItemDefBuilder<I> = also { properties.containerItem(container) }

    fun withGroup(group: ItemGroup): ItemDefBuilder<I> = also { properties.group(group) }

    fun withRarity(rarity: Rarity): ItemDefBuilder<I> = also { properties.rarity(rarity) }

    fun markUnrepairable(): ItemDefBuilder<I> = also { properties.setNoRepair() }

    fun withToolClass(toolClass: ToolType, harvestLevel: Int): ItemDefBuilder<I> = also {
        properties.addToolType(toolClass, harvestLevel)
    }

    fun withRenderer(rendererFactory: () -> ItemStackTileEntityRenderer): ItemDefBuilder<I> = also {
        properties.setTEISR { Callable { rendererFactory() } }
    }

    abstract fun build(): I

}

internal class ItemDefBuilderImpl<I : Item>(
        private val name: ResourceLocation,
        private val itemFactory: (Item.Properties) -> I
) : ItemDefBuilder<I>() {

    override fun build(): I = itemFactory(properties).also { it.registryName = name }

}
