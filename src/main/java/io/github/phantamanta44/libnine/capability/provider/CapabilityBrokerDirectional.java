package io.github.phantamanta44.libnine.capability.provider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class CapabilityBrokerDirectional implements ICapabilityProvider {

    private final Map<EnumFacing, Map<Capability<?>, Object>> capabilities;
    private final Map<Capability<?>, Object> nonDirectionalCapabilities;

    public CapabilityBrokerDirectional() {
        this.capabilities = new EnumMap<>(EnumFacing.class);
        this.nonDirectionalCapabilities = new IdentityHashMap<>();
    }

    public <T> CapabilityBrokerDirectional with(@Nullable EnumFacing dir, Capability<T> capability, T aspect) {
        if (dir != null) {
            capabilities.computeIfAbsent(dir, k -> new IdentityHashMap<>()).put(capability, aspect);
        } else {
            return with(capability, aspect);
        }
        return this;
    }

    public <T> CapabilityBrokerDirectional with(Capability<T> capability, T aspect) {
        nonDirectionalCapabilities.put(capability, aspect);
        return this;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (facing != null) {
            Map<Capability<?>, Object> dirCaps = capabilities.get(facing);
            if (dirCaps != null && dirCaps.containsKey(capability)) {
                return true;
            }
        }
        return nonDirectionalCapabilities.containsKey(capability);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (facing != null) {
            Map<Capability<?>, Object> dirCaps = capabilities.get(facing);
            if (dirCaps != null) {
                Object impl = dirCaps.get(capability);
                if (impl != null) {
                    return (T)impl;
                }
            }
        }
        return (T)nonDirectionalCapabilities.get(capability);
    }

}
