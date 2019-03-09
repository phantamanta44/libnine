package xyz.phanta.libnine.worldgen

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.IChunkGenSettings
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.IFeatureConfig
import net.minecraft.world.gen.feature.NoFeatureConfig
import net.minecraft.world.gen.placement.BasePlacement
import net.minecraft.world.gen.placement.NoPlacementConfig
import net.minecraftforge.registries.ForgeRegistries
import java.util.*

abstract class BiomeSet {

    val biomes: List<Biome> by lazy { ForgeRegistries.BIOMES.values.filter { contains(it) } }

    abstract fun contains(biome: Biome): Boolean

    infix fun and(other: BiomeSet): BiomeSet = ConjoinedBiomeSet(this, other)

    private class ConjoinedBiomeSet(private vararg val delegates: BiomeSet) : BiomeSet() {

        override fun contains(biome: Biome): Boolean = delegates.any { it.contains(biome) }

    }

    class Category(private val category: Biome.Category) : BiomeSet() {

        override fun contains(biome: Biome): Boolean = biome.category == category

        companion object {
            val NONE: BiomeSet = Category(Biome.Category.NONE)
            val TAIGA: BiomeSet = Category(Biome.Category.TAIGA)
            val EXTREME_HILLS: BiomeSet = Category(Biome.Category.EXTREME_HILLS)
            val JUNGLE: BiomeSet = Category(Biome.Category.JUNGLE)
            val MESA: BiomeSet = Category(Biome.Category.MESA)
            val PLAINS: BiomeSet = Category(Biome.Category.PLAINS)
            val SAVANNA: BiomeSet = Category(Biome.Category.SAVANNA)
            val ICY: BiomeSet = Category(Biome.Category.ICY)
            val THEEND: BiomeSet = Category(Biome.Category.THEEND)
            val BEACH: BiomeSet = Category(Biome.Category.BEACH)
            val FOREST: BiomeSet = Category(Biome.Category.FOREST)
            val OCEAN: BiomeSet = Category(Biome.Category.OCEAN)
            val DESERT: BiomeSet = Category(Biome.Category.DESERT)
            val RIVER: BiomeSet = Category(Biome.Category.RIVER)
            val SWAMP: BiomeSet = Category(Biome.Category.SWAMP)
            val MUSHROOM: BiomeSet = Category(Biome.Category.MUSHROOM)
            val NETHER: BiomeSet = Category(Biome.Category.NETHER)
        }

    }

    class Temperature(private val category: Biome.TempCategory) : BiomeSet() {

        override fun contains(biome: Biome): Boolean = biome.tempCategory == category

        companion object {
            val OCEAN: BiomeSet = Temperature(Biome.TempCategory.OCEAN)
            val COLD: BiomeSet = Temperature(Biome.TempCategory.COLD)
            val MEDIUM: BiomeSet = Temperature(Biome.TempCategory.MEDIUM)
            val WARM: BiomeSet = Temperature(Biome.TempCategory.WARM)
        }

    }

    class Rain(private val type: Biome.RainType) : BiomeSet() {

        override fun contains(biome: Biome): Boolean = biome.precipitation == type

        companion object {
            val NONE: BiomeSet = Rain(Biome.RainType.NONE)
            val RAIN: BiomeSet = Rain(Biome.RainType.RAIN)
            val SNOW: BiomeSet = Rain(Biome.RainType.SNOW)
        }

    }

}

abstract class NineFeature {

    internal fun buildFeature(): Feature<NoFeatureConfig> = FeatureImpl(this)

    abstract fun generate(
            world: IWorld,
            chunkGen: IChunkGenerator<out IChunkGenSettings>,
            origin: BlockPos,
            rand: Random
    ): Boolean

    private class FeatureImpl(private val parent: NineFeature) : Feature<NoFeatureConfig>() {

        override fun func_212245_a(
                world: IWorld,
                chunkGen: IChunkGenerator<out IChunkGenSettings>,
                rand: Random,
                pos: BlockPos,
                config: NoFeatureConfig
        ): Boolean = parent.generate(world, chunkGen, pos, rand)

    }

}

abstract class NineFeatureDistribution {

    internal fun buildDistribution(): BasePlacement<NoPlacementConfig> = PlacementImpl(this)

    abstract fun computeDistribution(
            world: IWorld,
            chunkGen: IChunkGenerator<out IChunkGenSettings>,
            origin: BlockPos,
            rand: Random
    ): List<BlockPos>

    private class PlacementImpl(private val parent: NineFeatureDistribution) : BasePlacement<NoPlacementConfig>() {

        override fun <C : IFeatureConfig?> generate(
                world: IWorld,
                chunkGen: IChunkGenerator<out IChunkGenSettings>,
                rand: Random,
                pos: BlockPos,
                config: NoPlacementConfig,
                feature: Feature<C>,
                featureConfig: C
        ): Boolean = parent.computeDistribution(world, chunkGen, pos, rand).map {
            feature.func_212245_a(world, chunkGen, rand, it, featureConfig)
        }.any()

    }

}
