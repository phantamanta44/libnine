package io.github.phantamanta44.libnine.capability.provider;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class CapabilityBrokerDirectional implements ICapabilityProvider {

    private final Table<EnumFacing, Capability, Object> capabilities;

    public CapabilityBrokerDirectional() {
        this.capabilities = Tables.newCustomTable(new HashMap<>(), HashMap::new);
    }

    public <T> CapabilityBrokerDirectional with(@Nullable EnumFacing dir, Capability<T> capability, T aspect) {
        capabilities.put(dir, capability, aspect);
        return this;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilities.contains(facing, capability);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T)capabilities.get(facing, capability);
    }

}
