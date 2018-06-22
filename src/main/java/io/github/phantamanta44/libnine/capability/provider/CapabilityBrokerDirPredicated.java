package io.github.phantamanta44.libnine.capability.provider;

import io.github.phantamanta44.libnine.util.tuple.IPair;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class CapabilityBrokerDirPredicated implements ICapabilityProvider {

    private final Map<Capability, IPair<BiPredicate, Object>> capabilities;

    public CapabilityBrokerDirPredicated() {
        this.capabilities = new HashMap<>();
    }

    public <T> void put(Capability<T> capability, T aspect, BiPredicate<T, EnumFacing> condition) {
        capabilities.put(capability, IPair.of(condition, aspect));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        IPair<BiPredicate, Object> entry = capabilities.get(capability);
        return entry != null && entry.getA().test(entry.getB(), facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        IPair<BiPredicate, Object> entry = capabilities.get(capability);
        return (entry != null && entry.getA().test(entry.getB(), facing)) ? (T)entry.getB() : null;
    }

}
