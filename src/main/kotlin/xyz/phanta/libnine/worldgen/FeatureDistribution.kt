package xyz.phanta.libnine.worldgen

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.gen.GenerationSettings
import net.minecraft.world.gen.ChunkGenerator
import xyz.phanta.libnine.util.math.clamp
import java.util.*
import kotlin.math.floor

class FeatureDistributionUniform(private val count: Int, private val minHeight: Int, private val variation: Int)
    : NineFeatureDistribution() {

    override fun computeDistribution(
            world: IWorld,
            chunkGen: ChunkGenerator<out GenerationSettings>,
            origin: BlockPos,
            rand: Random
    ): List<BlockPos> = (1..count).map {
        origin.add(rand.nextInt(16), minHeight + rand.nextInt(variation + 1), rand.nextInt(16))
    }

}

class FeatureDistributionNormal(private val count: Int, private val mean: Double, private val variation: Double)
    : NineFeatureDistribution() {

    override fun computeDistribution(
            world: IWorld,
            chunkGen: ChunkGenerator<out GenerationSettings>,
            origin: BlockPos,
            rand: Random
    ): List<BlockPos> = (1..count).map {
        origin.add(
                rand.nextInt(16),
                floor((rand.nextGaussian() * variation / 2).clamp(-variation, variation) + mean).toInt().clamp(0, chunkGen.maxHeight),
                rand.nextInt(16)
        )
    }

}

class FeatureDistributionSparse(private val probability: Double, private val delegate: NineFeatureDistribution)
    : NineFeatureDistribution() {

    override fun computeDistribution(
            world: IWorld,
            chunkGen: ChunkGenerator<out GenerationSettings>,
            origin: BlockPos,
            rand: Random
    ): List<BlockPos> = if (rand.nextDouble() < probability) {
        delegate.computeDistribution(world, chunkGen, origin, rand)
    } else {
        Collections.emptyList()
    }

}
