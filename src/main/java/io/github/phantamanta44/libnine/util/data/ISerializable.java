package io.github.phantamanta44.libnine.util.data;

import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;

public interface ISerializable {

    void serializeNBT(NBTTagCompound tag);

    void deserializeNBT(NBTTagCompound tag);

    void serializeBytes(ByteUtils.Writer data);

    void deserializeBytes(ByteUtils.Reader data);

}