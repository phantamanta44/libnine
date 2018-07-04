package io.github.phantamanta44.libnine.util.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;

public class WorldBlockPos {

    private final BlockPos pos;
    private final World world;

    public WorldBlockPos(World world, int x, int y, int z) {
        this.pos = new BlockPos(x, y, z).toImmutable();
        this.world = world;
    }

    public WorldBlockPos(int dim, int x, int y, int z) {
        this(DimensionManager.getWorld(dim), x, y, z);
    }

    public WorldBlockPos(World world, BlockPos pos) {
        this(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public WorldBlockPos(int dim, BlockPos pos) {
        this(dim, pos.getX(), pos.getY(), pos.getZ());
    }

    public World getWorld() {
        return world;
    }

    public int getDimId() {
        return world.provider.getDimension();
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }

    @Nullable
    public TileEntity getTileEntity() {
        return world.getTileEntity(pos);
    }

    public IBlockState getBlockState() {
        return getWorld().getBlockState(pos);
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorldBlockPos && pos.equals(((WorldBlockPos)o).pos)
                && getDimId() == ((WorldBlockPos)o).getDimId();
    }

    @Override
    public int hashCode() {
        return world.hashCode() ^ pos.hashCode();
    }

}
