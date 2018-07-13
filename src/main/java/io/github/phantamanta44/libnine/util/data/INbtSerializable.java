package io.github.phantamanta44.libnine.util.data;

import net.minecraft.nbt.NBTTagCompound;

public interface INbtSerializable {

    void serNBT(NBTTagCompound tag);

    void deserNBT(NBTTagCompound tag);

}
