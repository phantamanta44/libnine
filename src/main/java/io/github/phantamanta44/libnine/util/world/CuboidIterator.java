package io.github.phantamanta44.libnine.util.world;

import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CuboidIterator implements Iterator<BlockPos> {

    private int minX, minZ, maxX, maxY, maxZ;
    private int nextX, nextY, nextZ;

    public CuboidIterator(BlockPos min, BlockPos max) {
        this.minX = min.getX();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();
        this.nextX = minX;
        this.nextY = min.getY();
        this.nextZ = minZ;
    }

    @Override
    public boolean hasNext() {
        return nextX <= maxX && nextY <= maxY && nextZ <= maxZ;
    }

    @Override
    public BlockPos next() {
        if (hasNext()) {
            BlockPos nextPos = new BlockPos(nextX, nextY, nextZ);
            if (++nextX > maxX) {
                nextX = minX;
                if (++nextZ > maxZ) {
                    nextZ = minZ;
                    ++nextY;
                }
            }
            return nextPos;
        }
        throw new NoSuchElementException();
    }

}
