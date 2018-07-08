package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.component.reservoir.IIntReservoir;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

public class L9AspectEnergy implements IEnergyStorage, ISerializable {

    private final IIntReservoir reservoir;

    public L9AspectEnergy(IIntReservoir reservoir) {
        this.reservoir = reservoir;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return reservoir.offer(maxReceive, !simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return reservoir.draw(maxExtract, !simulate);
    }

    @Override
    public int getEnergyStored() {
        return reservoir.getQuantity();
    }

    @Override
    public int getMaxEnergyStored() {
        return reservoir.getCapacity();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        reservoir.serializeNBT(tag);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        reservoir.deserializeNBT(tag);
    }

    @Override
    public void serializeBytes(ByteUtils.Writer data) {
        reservoir.serializeBytes(data);
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        reservoir.deserializeBytes(data);
    }

}
