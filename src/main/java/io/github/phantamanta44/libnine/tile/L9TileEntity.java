package io.github.phantamanta44.libnine.tile;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class L9TileEntity extends TileEntity implements ISerializable {

    private boolean requiresSync;

    public L9TileEntity() {
        requiresSync = false;
    }

    /*
     * Initializers
     */

    protected void markRequiresSync() {
        requiresSync = true;
    }

    /*
     * Behaviour
     */

    protected void setDirty() {
        markDirty();
        if (!getWorld().isRemote) dispatchTileUpdate();
    }

    /**
     * Please don't touch this :(
     */
    @Override
    @Deprecated
    public void markDirty() {
        super.markDirty();
    }

    protected void dispatchTileUpdate() {
        if (requiresSync) LibNine.PROXY.dispatchTileUpdate(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        writeToNBT(tag);
    }

    @Override
    public void serializeBytes(ByteUtils.Writer data) {
        // NO-OP
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        // NO-OP
    }

}
