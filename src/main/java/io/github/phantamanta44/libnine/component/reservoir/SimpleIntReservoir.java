package io.github.phantamanta44.libnine.component.reservoir;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.function.IIntBiConsumer;
import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;

public class SimpleIntReservoir implements IIntReservoir {

    private final int capacity;
    private final Collection<IIntBiConsumer> callbacks;

    private int qty;

    public SimpleIntReservoir(int qty, int capacity) {
        this.qty = qty;
        this.capacity = capacity;
        this.callbacks = new ArrayList<>();
    }

    public SimpleIntReservoir(int capacity) {
        this(0, capacity);
    }

    @Override
    public int getQuantity() {
        return qty;
    }

    @Override
    public void setQuantity(int qty) {
        qty = MathUtils.clamp(qty, 0, getCapacity());
        if (qty != this.qty) {
            int oldQty = this.qty;
            this.qty = qty;
            for (IIntBiConsumer callback : callbacks) {
                callback.accept(oldQty, qty);
            }
        }
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int draw(int amount, boolean notSimulated) {
        int toTransfer = Math.min(amount, getQuantity());
        if (notSimulated) offsetQuantity(-toTransfer);
        return toTransfer;
    }

    @Override
    public int offer(int amount, boolean notSimulated) {
        int toTransfer = Math.min(amount, getRemainingCapacity());
        if (notSimulated) offsetQuantity(toTransfer);
        return toTransfer;
    }

    @Override
    public void onQuantityChange(IIntBiConsumer callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        tag.setInteger("Quantity", getQuantity());
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        setQuantity(tag.getInteger("Quantity"));
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        data.writeInt(getQuantity());
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        setQuantity(data.readInt());
    }

}
