package io.github.phantamanta44.libnine.util.world;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.EnumMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class SideAlloc<E extends Enum<E>> implements IAllocableSides<E>, ISerializable {

    private final Class<E> enumType;
    private final Supplier<EnumFacing> frontGetter;
    private final EnumMap<BlockSide, E> faces;

    public SideAlloc(E defaultState, Supplier<EnumFacing> frontGetter) {
        this.enumType = defaultState.getDeclaringClass();
        this.frontGetter = frontGetter;
        this.faces = new EnumMap<>(BlockSide.class);
        for (BlockSide face : BlockSide.values()) this.faces.put(face, defaultState);
    }

    @Override
    public void setFace(BlockSide face, E state) {
        faces.put(face, state);
    }

    @Override
    public E getFace(BlockSide face) {
        return faces.get(face);
    }

    public <T> BiPredicate<T, EnumFacing> getPredicate(E state) {
        return (t, f) -> faces.get(BlockSide.fromDirection(frontGetter.get(), f)) == state;
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        for (BlockSide side : BlockSide.values()) data.writeShort((short)faces.get(side).ordinal());
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        E[] values = enumType.getEnumConstants();
        for (BlockSide side : BlockSide.values()) faces.put(side, values[(data.readShort())]);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        for (BlockSide side : BlockSide.values()) tag.setString(side.name(), faces.get(side).toString());
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        for (BlockSide side : BlockSide.values()) faces.put(side, Enum.valueOf(enumType, tag.getString(side.name())));
    }

}
