package xyz.phanta.libnine.util.world

import net.minecraft.entity.item.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.network.PacketDistributor
import xyz.phanta.libnine.util.orNull

operator fun BlockPos.plus(other: Vec3i): BlockPos = this.add(other)
operator fun BlockPos.plus(dir: Direction): BlockPos = this.offset(dir)
operator fun BlockPos.minus(other: Vec3i): BlockPos = this.subtract(other)
operator fun BlockPos.minus(dir: Direction): BlockPos = this.offset(dir.opposite)

fun BlockPos.walk(dir: Direction): Sequence<BlockPos> = generateSequence(this + dir) { it + dir }

fun World.dropItem(pos: Vec3d, stack: ItemStack) {
    if (!this.isRemote && !stack.isEmpty) {
        val ent = ItemEntity(this, pos.x, pos.y, pos.z, stack)
        ent.setMotion(
                this.rand.nextGaussian() * 0.05,
                this.rand.nextGaussian() * 0.05 + 0.2,
                this.rand.nextGaussian() * 0.05
        )
        ent.setDefaultPickupDelay()
        this.addEntity(ent)
    }
}

fun World.dropItem(pos: BlockPos, stack: ItemStack) {
    if (!world.isRemote && !stack.isEmpty) {
        val ent = ItemEntity(world,
                pos.x.toDouble() + (world.rand.nextFloat() * 0.5f).toDouble() + 0.25,
                pos.y.toDouble() + (world.rand.nextFloat() * 0.5f).toDouble() + 0.25,
                pos.z.toDouble() + (world.rand.nextFloat() * 0.5f).toDouble() + 0.25, stack)
        ent.setMotion(
                this.rand.nextGaussian() * 0.05,
                this.rand.nextGaussian() * 0.05 + 0.2,
                this.rand.nextGaussian() * 0.05
        )
        ent.setDefaultPickupDelay()
        world.addEntity(ent)
    }
}

fun World.getPacketRange(pos: BlockPos, dist: Double): PacketDistributor.TargetPoint =
        PacketDistributor.TargetPoint(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), dist, this.dimension.type)

fun TileEntity.adjacentTiles(): List<TileEntity> = enumValues<Direction>().mapNotNull { this.getAdjacentTile(it) }

fun TileEntity.getAdjacentTile(dir: Direction): TileEntity? = this.world!!.getTileEntity(this.pos + dir)

fun <T : Any> TileEntity.adjacentCaps(cap: Capability<T>): List<T> =
        enumValues<Direction>().mapNotNull { getAdjacentCap(it, cap) }

fun <T> TileEntity.getAdjacentCap(dir: Direction, cap: Capability<T>): T? =
        getAdjacentTile(dir)?.getCapability(cap, dir.opposite)?.orNull()

fun BlockPos.getCenter(): Vec3d = Vec3d(this.x + 0.5, this.y + 0.5, this.z + 0.5)

fun BlockPos.containsPoint(point: Vec3d): Boolean =
        point.x >= this.x && point.x <= this.x + 1
                && point.y >= this.y && point.y <= this.y + 1
                && point.z >= this.z && point.z <= this.z + 1
