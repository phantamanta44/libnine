package io.github.phantamanta44.libnine.capability.provider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public class CapabilityBroker implements ICapabilityProvider {

    private final Map<Capability<?>, Object> capabilities;

    public CapabilityBroker() {
        this.capabilities = new IdentityHashMap<>();
    }

    public <T> CapabilityBroker with(Capability<T> capability, T aspect) {
        capabilities.put(capability, aspect);
        return this;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilities.containsKey(capability);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T)capabilities.get(capability);
    }

}
