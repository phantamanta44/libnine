package io.github.phantamanta44.libnine.component.reservoir;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.function.IIntBiConsumer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class DelegatedIntReservoir implements IIntReservoir {

    private final IIntReservoir backing;

    public DelegatedIntReservoir(IIntReservoir backing) {
        this.backing = backing;
    }

    @Override
    public int getQuantity() {
        return backing.getQuantity();
    }

    @Override
    public void setQuantity(int qty) {
        backing.setQuantity(qty);
    }

    @Override
    public void offsetQuantity(int offset) {
        backing.offsetQuantity(offset);
    }

    @Override
    public int getCapacity() {
        return backing.getCapacity();
    }

    @Override
    public int getRemainingCapacity() {
        return backing.getRemainingCapacity();
    }

    @Override
    public int draw(int amount, boolean notSimulated) {
        return backing.draw(amount, notSimulated);
    }

    @Override
    public int offer(int amount, boolean notSimulated) {
        return backing.offer(amount, notSimulated);
    }

    @Override
    public void onQuantityChange(IIntBiConsumer callback) {
        backing.onQuantityChange(callback);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        backing.serNBT(tag);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        backing.deserNBT(tag);
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        backing.serBytes(data);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        backing.deserBytes(data);
    }

}
