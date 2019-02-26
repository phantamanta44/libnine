package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.IForgeRegistryEntry
import xyz.phanta.libnine.Virtue
import java.util.function.Consumer

private fun <T : IForgeRegistryEntry<T>> newQueue(bus: IEventBus): MutableList<T> = mutableListOf<T>().also {
    bus.addListener(Consumer<RegistryEvent.Register<T>> { e -> it.forEach(e.registry::register) })
}

class Registrar(internal val mod: Virtue, bus: IEventBus) {

    internal val blocks: MutableList<Block> = newQueue(bus)
    internal val items: MutableList<Item> = newQueue(bus)
    internal val tileEntities: MutableList<TileEntityType<*>> = newQueue(bus)
    private val toClear: MutableList<MutableList<*>> = mutableListOf()

    // TODO potions
    // TODO biomes
    // TODO soundevents
    // TODO potiontypes
    // TODO enchantments
    // TODO professions
    // TODO entities
    // TODO mod dimensions

}
