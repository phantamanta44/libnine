package io.github.phantamanta44.libnine.util.world;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.Iterator;

public class DirectionToggle implements Iterable<EnumFacing>, ISerializable {

    private final boolean[] dirs = new boolean[EnumFacing.VALUES.length];

    public boolean get(EnumFacing dir) {
        return dirs[dir.ordinal()];
    }

    public void set(EnumFacing dir, boolean value) {
        dirs[dir.ordinal()] = value;
    }

    public boolean toggle(EnumFacing dir) {
        return dirs[dir.ordinal()] = !dirs[dir.ordinal()];
    }

    public void clear() {
        Arrays.fill(dirs, false);
    }

    @Override
    public Iterator<EnumFacing> iterator() {
        return Arrays.stream(EnumFacing.VALUES).filter(d -> dirs[d.ordinal()]).iterator();
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        byte mask = 0;
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dirs[dir.ordinal()]) {
                mask |= 1 << dir.ordinal();
            }
        }
        data.writeByte(mask);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        byte mask = data.readByte();
        for (EnumFacing dir : EnumFacing.VALUES) {
            dirs[dir.ordinal()] = (mask & (1 << dir.ordinal())) != 0;
        }
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            tag.setBoolean(dir.getName(), dirs[dir.ordinal()]);
        }
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            dirs[dir.ordinal()] = tag.getBoolean(dir.getName());
        }
    }

}
