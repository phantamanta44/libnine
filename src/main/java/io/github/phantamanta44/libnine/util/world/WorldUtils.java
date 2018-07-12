package io.github.phantamanta44.libnine.util.world;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class WorldUtils {

    public static void dropItem(World world, Vec3d pos, ItemStack stack) {
        if (!world.isRemote && !stack.isEmpty()) {
            EntityItem ent = new EntityItem(world, pos.x, pos.y, pos.z, stack);
            ent.motionX = world.rand.nextGaussian() * 0.05D;
            ent.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
            ent.motionZ = world.rand.nextGaussian() * 0.05D;
            ent.setDefaultPickupDelay();
            world.spawnEntity(ent);
        }
    }

    public static void dropItem(World world, BlockPos pos, ItemStack stack) {
        if (!world.isRemote && !stack.isEmpty()) {
            EntityItem ent = new EntityItem(world,
                    pos.getX() + world.rand.nextFloat() * 0.5F + 0.25D,
                    pos.getY() + world.rand.nextFloat() * 0.5F + 0.25D,
                    pos.getZ() + world.rand.nextFloat() * 0.5F + 0.25D, stack);
            ent.motionX = world.rand.nextGaussian() * 0.05D;
            ent.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
            ent.motionZ = world.rand.nextGaussian() * 0.05D;
            ent.setDefaultPickupDelay();
            world.spawnEntity(ent);
        }
    }

    public static void dropItem(WorldBlockPos pos, ItemStack stack) {
        dropItem(pos.getWorld(), pos.getPos(), stack);
    }

    public static void iterateAdjacentTiles(TileEntity tile, BiConsumer<TileEntity, EnumFacing> func) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            TileEntity adj = getAdjacentTile(tile, dir);
            if (adj != null) func.accept(adj, dir);
        }
    }

    @Nullable
    public static TileEntity getAdjacentTile(TileEntity base, EnumFacing dir) {
        return base.getWorld().getTileEntity(base.getPos().add(dir.getDirectionVec()));
    }

    public static Vec3d getBlockCenter(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }
    
    public static boolean pointLiesInBlock(Vec3d point, BlockPos block) {
        return point.x >= block.getX() && point.x <= block.getX() + 1
                && point.y >= block.getY() && point.y <= block.getY() + 1
                && point.z >= block.getZ() && point.z <= block.getZ() + 1;
    }



}
