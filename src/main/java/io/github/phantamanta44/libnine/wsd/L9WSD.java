package io.github.phantamanta44.libnine.wsd;

import io.github.phantamanta44.libnine.Virtue;
import io.github.phantamanta44.libnine.util.data.INbtSerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

public abstract class L9WSD extends WorldSavedData implements INbtSerializable {

    public L9WSD(Virtue virtue, IWSDIdentity identity) {
        super(String.format("%s:%s_%s", virtue.getModId(), identity.getPrefix(), identity.getIdentifier()));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        deserNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        serNBT(nbt);
        return nbt;
    }

}
