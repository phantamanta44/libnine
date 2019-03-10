package xyz.phanta.libnine.worldgen

import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IWorld
import net.minecraft.world.gen.IChunkGenSettings
import net.minecraft.world.gen.IChunkGenerator
import xyz.phanta.libnine.util.math.*
import java.util.*

class SubterraneanFeatureClump(
        private val lengthMin: Int,
        private val lengthVariation: Int,
        private val radiusMin: Int,
        private val radiusVariation: Int,
        private val blockProvider: () -> IBlockState,
        private val replacementTest: (IBlockState) -> Boolean
) : NineFeature() {

    override fun generate(world: IWorld, chunkGen: IChunkGenerator<out IChunkGenSettings>, origin: BlockPos, rand: Random): Boolean {
        val length = lengthMin + rand.nextInt(lengthVariation + 1)
        val radius = radiusMin + rand.nextInt(radiusVariation + 1)
        val dir = Vec3d(rand.nextDouble(), rand.nextDouble().randomNonZero(), rand.nextDouble()).normalize()
        val basisU = dir.findOrthogonal().normalize()
        val basisV = (dir cross basisU).normalize()
        val endpoint = Vec3d(origin) - dir * (length / 2.0)
        var success = false
        for (offset in 0..length) {
            val centre = endpoint + dir * offset.toDouble()
            for (u in -radius..radius) {
                for (v in -radius..radius) {
                    val point = BlockPos(centre + basisU * u.toDouble() + basisV * v.toDouble())
                    if (world.getBlockState(point).isReplaceableOreGen(world.world, point, replacementTest)) {
                        world.setBlockState(point, blockProvider(), 2)
                        success = true
                    }
                }
            }
        }
        return success
    }

}

class SubterraneanFeaturePath(
        private val lengthMin: Int,
        private val lengthVariation: Int,
        private val deviance: Double,
        private val blockProvider: () -> IBlockState,
        private val replacementTest: (IBlockState) -> Boolean
) : NineFeature() {

    override fun generate(world: IWorld, chunkGen: IChunkGenerator<out IChunkGenSettings>, origin: BlockPos, rand: Random): Boolean {
        var pos = Vec3d(origin)
        var dir = Vec3d(rand.nextDouble(), rand.nextDouble().randomNonZero(), rand.nextDouble()).normalize()
        var success = false
        for (i in 1..(lengthMin + rand.nextInt(lengthVariation + 1))) {
            pos += dir
            val blockPos = BlockPos(pos)
            if (world.getBlockState(blockPos).isReplaceableOreGen(world.world, blockPos, replacementTest)) {
                world.setBlockState(blockPos, blockProvider(), 2)
                success = true
            }
            val axis = dir.findOrthogonal().rotateOrthogonal(dir, rand.nextDouble() * TWO_PI)
            dir = dir.rotateOrthogonal(axis, rand.nextDouble() * TWO_PI * deviance)
        }
        return success
    }

}

class SubterraneanFeatureRadiate(
        private val radiusMin: Int,
        private val radiusVariation: Int,
        private val blockProvider: () -> IBlockState,
        private val replacementTest: (IBlockState) -> Boolean
) : NineFeature() {

    override fun generate(world: IWorld, chunkGen: IChunkGenerator<out IChunkGenSettings>, origin: BlockPos, rand: Random): Boolean {
        val radius = radiusMin + rand.nextInt(radiusVariation + 1)
        val radiusSq = radius * radius
        var success = false
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                for (k in -radius..radius) {
                    val distSq = i * i + j * j + k * k
                    if (distSq <= radiusSq && (distSq == 0 || rand.nextDouble() > distSq / radiusSq)) {
                        val pos = origin.add(i, j, k)
                        if (world.getBlockState(pos).isReplaceableOreGen(world.world, pos, replacementTest)) {
                            world.setBlockState(pos, blockProvider(), 2)
                            success = true
                        }
                    }
                }
            }
        }
        return success
    }

}
