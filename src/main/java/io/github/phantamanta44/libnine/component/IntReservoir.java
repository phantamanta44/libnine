package io.github.phantamanta44.libnine.component;

import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.function.IIntBiConsumer;
import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.LinkedList;

public class IntReservoir implements ISerializable {

    private final int capacity;
    private final Collection<IIntBiConsumer> callbacks;

    private int qty;

    public IntReservoir(int qty, int capacity) {
        this.qty = qty;
        this.capacity = capacity;
        this.callbacks = new LinkedList<>();
    }

    public IntReservoir(int capacity) {
        this(0, capacity);
    }

    public int getQuantity() {
        return qty;
    }

    public void setQuantity(int qty) {
        int oldQty = this.qty;
        this.qty = MathUtils.clamp(qty, 0, getCapacity());
        callbacks.forEach(c -> c.accept(oldQty, this.qty));
    }

    public void offsetQuantity(int offset) {
        setQuantity(getQuantity() + offset);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRemainingCapacity() {
        return getCapacity() - getQuantity();
    }

    public int draw(int amount, boolean notSimulated) {
        int toTransfer = Math.min(amount, getQuantity());
        if (notSimulated) offsetQuantity(-toTransfer);
        return toTransfer;
    }

    public int offer(int amount, boolean notSimulated) {
        int toTransfer = Math.min(amount, getRemainingCapacity());
        if (notSimulated) offsetQuantity(toTransfer);
        return toTransfer;
    }

    public void onQuantityChange(IIntBiConsumer callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        tag.setInteger("Quantity", getQuantity());
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        setQuantity(tag.getInteger("Quantity"));
    }

    @Override
    public void serializeBytes(ByteUtils.Writer data) {
        data.writeInt(getQuantity());
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        setQuantity(data.readInt());
    }

}
