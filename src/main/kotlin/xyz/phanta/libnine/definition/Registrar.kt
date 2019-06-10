package xyz.phanta.libnine.definition

import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.Item
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.particles.ParticleType
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.SoundEvent
import net.minecraft.world.gen.GenerationStage
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import xyz.phanta.libnine.RegistryHandler
import xyz.phanta.libnine.Virtue
import xyz.phanta.libnine.client.fx.NineParticleType
import xyz.phanta.libnine.worldgen.BiomeSet

open class Registrar internal constructor(internal val mod: Virtue, internal val bus: IEventBus, private val regHandler: RegistryHandler) {

    internal val blocks: MutableList<Block> = newQueue()
    internal val items: MutableList<Item> = newQueue()
    internal val tileEntities: MutableList<TileEntityType<*>> = newQueue()
    internal val soundEvents: MutableList<SoundEvent> = newQueue()
    internal val containerTypes: MutableList<ContainerType<*>> = newQueue()
    internal val recipeSerializers: MutableList<IRecipeSerializer<*>> = newQueue()
    internal val particles: MutableList<NineParticleType<*>> = mutableListOf()
    internal val features: MutableList<Triple<ConfiguredFeature<*>, GenerationStage.Decoration, BiomeSet>> = mutableListOf()

    // TODO wrap other forge registries

    init {
        // FIXME probably should register using forge registries somehow
        regHandler.registerProvider<Enchantment> {
            features.forEach { (feature, stage, biomes) -> biomes.biomes.forEach { it.addFeature(stage, feature) } }
        }
        regHandler.registerProvider<ParticleType<*>> { reg -> particles.forEach { reg.register(it.type) } }
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified T : IForgeRegistryEntry<T>> newQueue(): MutableList<T> =
            mutableListOf<T>().also {
                regHandler.registerProvider { reg: IForgeRegistry<T> ->
                    it.forEach(reg::register)
                }
            }

}
