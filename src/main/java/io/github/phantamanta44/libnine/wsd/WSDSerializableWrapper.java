package io.github.phantamanta44.libnine.wsd;

import io.github.phantamanta44.libnine.Virtue;
import io.github.phantamanta44.libnine.util.data.INbtSerializable;
import net.minecraft.nbt.NBTTagCompound;

public class WSDSerializableWrapper<T extends INbtSerializable> extends L9WSD {

    private T value;

    public WSDSerializableWrapper(Virtue virtue, IWSDIdentity identity, T initialValue) {
        super(virtue, identity);
        this.value = initialValue;
    }

    public WSDSerializableWrapper(Virtue virtue, IWSDIdentity identity) {
        this(virtue, identity, null);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        markDirty();
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        value.serNBT(tag);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        value.deserNBT(tag);
    }

}
