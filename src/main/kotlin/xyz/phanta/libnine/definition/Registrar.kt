package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntityType
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.CompositeFeature
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.worldgen.BiomeSet

class Registrar internal constructor(internal val mod: Virtue, internal val bus: IEventBus, private val regHandler: RegistryHandler) {

    internal val blocks: MutableList<Block> = newQueue()
    internal val items: MutableList<Item> = newQueue()
    internal val tileEntities: MutableList<TileEntityType<*>> = newQueue()
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
        regHandler.registerProvider<Enchantment> {
            features.forEach { (feature, stage, biomes) -> biomes.biomes.forEach { it.addFeature(stage, feature) } }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : IForgeRegistryEntry<T>> newQueue(): MutableList<T> =
            mutableListOf<T>().also {
                regHandler.registerProvider { reg: IForgeRegistry<T> ->
                    it.forEach(reg::register)
                }
            }

}
