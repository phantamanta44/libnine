package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntityType
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.CompositeFeature
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.IForgeRegistryEntry
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.worldgen.BiomeSet
import java.util.function.Consumer

private inline fun <reified T : IForgeRegistryEntry<T>> newQueue(bus: IEventBus): MutableList<T> = mutableListOf<T>().also {
    bus.addGenericListener(T::class.java) { e: RegistryEvent.Register<T> -> it.forEach(e.registry::register) }
}

class Registrar(internal val mod: Virtue, internal val bus: IEventBus) {

    internal val blocks: MutableList<Block> = newQueue(bus)
    internal val items: MutableList<Item> = newQueue(bus)
    internal val tileEntities: MutableList<TileEntityType<*>> = newQueue(bus)
    internal val features: MutableList<Triple<CompositeFeature<*, *>, GenerationStage.Decoration, BiomeSet>> = mutableListOf()

    // TODO potions
    // TODO biomes
    // TODO soundevents
    // TODO potiontypes
    // TODO enchantments
    // TODO professions
    // TODO entities
    // TODO mod dimensions

    init {
        bus.addListener(EventPriority.LOWEST, Consumer<RegistryEvent.Register<Biome>> {
            features.forEach { (feature, stage, biomes) -> biomes.biomes.forEach { it.addFeature(stage, feature) } }
        })
    }

}
