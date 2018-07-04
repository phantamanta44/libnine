package io.github.phantamanta44.libnine.tile;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.capability.provider.NoopCapabilities;
import io.github.phantamanta44.libnine.util.LazyConstant;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.data.serialization.DataSerialization;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class L9TileEntity extends TileEntity implements ISerializable {

    private final DataSerialization serializer;
    private final LazyConstant<ICapabilityProvider> capabilityBroker;

    private WorldBlockPos worldPos;
    private boolean requiresSync;

    public L9TileEntity() {
        this.serializer = new DataSerialization(this);
        this.capabilityBroker = new LazyConstant<>(this::initCapabilities);
        this.worldPos = null;
        this.requiresSync = false;
    }

    /*
     * Initializers
     */

    protected ICapabilityProvider initCapabilities() {
        return new NoopCapabilities();
    }

    protected void markRequiresSync() {
        requiresSync = true;
    }

    /*
     * Properties
     */

    public WorldBlockPos getWorldPos() {
        return worldPos == null ? (worldPos = new WorldBlockPos(world, pos)) : worldPos;
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

    @Override
    public void setPos(BlockPos pos) {
        super.setPos(pos);
        if (world != null) worldPos = new WorldBlockPos(world, pos);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (pos != null) worldPos = new WorldBlockPos(world, pos);
    }

    protected void dispatchTileUpdate() {
        if (requiresSync) LibNine.PROXY.dispatchTileUpdate(this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilityBroker.get().hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        T aspect = capabilityBroker.get().getCapability(capability, facing);
        return aspect != null ? aspect : super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        worldPos = new WorldBlockPos(world, pos);
        serializer.deserializeNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        serializer.serializeNBT(compound);
        return compound;
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
        serializer.serializeBytes(data);
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        serializer.deserializeBytes(data);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return requiresSync ? new SPacketUpdateTileEntity(pos, -0, getUpdateTag()) : null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return serializeNBT();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

}
