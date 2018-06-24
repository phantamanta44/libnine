package io.github.phantamanta44.libnine.util;

import io.github.phantamanta44.libnine.util.helper.WorldUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;

public class WorldBlockPos extends BlockPos {

    private World world;

    public WorldBlockPos(World world, int x, int y, int z) {
        super(x, y, z);
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

    public void setWorld(World world) {
        this.world = world;
    }

    @Nullable
    public TileEntity getTileEntity() {
        return WorldUtils.getTileSafely(world, this);
    }

    public IBlockState getBlockState() {
        return getWorld().getBlockState(this);
    }

    public BlockPos demote() {
        return new BlockPos(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorldBlockPos && super.equals(o)
                && world.provider.getDimension() == ((WorldBlockPos)o).getWorld().provider.getDimension();
    }

}
