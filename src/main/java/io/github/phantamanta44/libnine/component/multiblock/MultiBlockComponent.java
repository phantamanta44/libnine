package io.github.phantamanta44.libnine.component.multiblock;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.nbt.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiBlockComponent<T extends IMultiBlockUnit<T>> extends MultiBlockConnectable<T> {

    @Nullable
    private BlockPos corePos = null;
    @Nullable
    private MultiBlockCore<T> cachedCore = null;
    private final List<Runnable> connectionStatusCallbacks = new ArrayList<>();

    public MultiBlockComponent(T component, MultiBlockType<T> type) {
        super(component, type);
    }

    @Nullable
    @Override
    public MultiBlockCore<T> getCore() {
        if (cachedCore != null) {
            corePos = null;
            return cachedCore;
        } else if (corePos != null) {
            cachedCore = (MultiBlockCore<T>)getAtPos(corePos);
            corePos = null;
        }
        return cachedCore;
    }

    @Override
    public void setCore(@Nullable MultiBlockCore<T> core) {
        this.cachedCore = core;
        postConnectionStatusChange();
    }

    public boolean isConnected() {
        return getCore() != null;
    }

    public void onConnectionStatusChange(Runnable callback) {
        connectionStatusCallbacks.add(callback);
    }

    private void postConnectionStatusChange() {
        connectionStatusCallbacks.forEach(Runnable::run);
    }

    @Override
    public void disconnect() {
        MultiBlockCore<T> core = getCore();
        if (core != null) {
            core.disconnect();
        }
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        super.serBytes(data);
        MultiBlockCore<T> core = getCore();
        if (core != null) {
            data.writeBool(true).writeBlockPos(core.getUnit().getWorldPos().getPos());
        } else {
            data.writeBool(false);
        }
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        super.deserBytes(data);
        cachedCore = null;
        if (data.readBool()) {
            corePos = data.readBlockPos();
        }
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        super.serNBT(tag);
        MultiBlockCore<T> core = getCore();
        if (core != null) {
            tag.setTag("CorePos", NBTUtils.serializeBlockPos(core.getUnit().getWorldPos().getPos()));
        }
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        super.deserNBT(tag);
        cachedCore = null;
        if (tag.hasKey("CorePos")) {
            corePos = NBTUtils.deserializeBlockPos(tag.getCompoundTag("CorePos"));
        }
    }

}
