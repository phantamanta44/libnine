package io.github.phantamanta44.libnine.component.multiblock;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.world.DirectionToggle;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;

public abstract class MultiBlockConnectable<T extends IMultiBlockUnit<T>>
        implements Iterable<MultiBlockConnectable<T>>, ISerializable {

    private final T unit;
    private final MultiBlockType<T> type;
    private final DirectionToggle childDirs = new DirectionToggle();

    public MultiBlockConnectable(T unit, MultiBlockType<T> type) {
        this.unit = unit;
        this.type = type;
    }

    public T getUnit() {
        return unit;
    }

    public MultiBlockType<T> getType() {
        return type;
    }

    @Nullable
    public abstract MultiBlockCore<T> getCore();

    public abstract void setCore(@Nullable MultiBlockCore<T> core);

    public Iterable<EnumFacing> getEmittingDirs() {
        return childDirs;
    }

    public MultiBlockConnectionResult tryEmit(EnumFacing dir) {
        MultiBlockConnectable<T> adjacent = getAdjacent(dir);
        if (adjacent == null) {
            return MultiBlockConnectionResult.NO_ADJACENT;
        }
        MultiBlockCore<T> adjCore = adjacent.getCore();
        if (adjCore == null) {
            childDirs.set(dir, true);
            adjacent.setCore(getCore());
            return MultiBlockConnectionResult.SUCCESS;
        } else if (adjCore == getCore()) {
            return MultiBlockConnectionResult.EXISTING_CONNECTION;
        } else {
            return MultiBlockConnectionResult.CONFLICT;
        }
    }

    public void clearEmission() {
        for (EnumFacing dir : childDirs) {
            MultiBlockConnectable<T> adjacent = getAdjacent(dir);
            if (adjacent != null) {
                adjacent.setCore(null);
            }
        }
        childDirs.clear();
    }

    public abstract void disconnect();

    @Nullable
    public MultiBlockConnectable<T> getAdjacent(EnumFacing dir) {
        return getAtPos(getUnit().getWorldPos().getPos().offset(dir));
    }

    @Nullable
    public MultiBlockConnectable<T> getAtPos(BlockPos pos) {
        T component = getType().checkComponent(getUnit().getWorldPos().getWorld().getTileEntity(pos));
        return component != null ? component.getMultiBlockConnection() : null;
    }

    @Override
    public Iterator<MultiBlockConnectable<T>> iterator() {
        return new MultiBlockIterator<>(this);
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        childDirs.serBytes(data);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        childDirs.deserBytes(data);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        NBTTagCompound dirsTag = new NBTTagCompound();
        childDirs.serNBT(dirsTag);
        tag.setTag("ChildDirs", dirsTag);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        childDirs.deserNBT(tag.getCompoundTag("ChildDirs"));
    }

}
