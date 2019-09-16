package io.github.phantamanta44.libnine.capability.provider;

import io.github.phantamanta44.libnine.util.tuple.IPair;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class CapabilityBrokerDirPredicated implements ICapabilityProvider {

    private final Map<Capability<?>, Collection<IPair<BiPredicate, ?>>> capabilities;

    public CapabilityBrokerDirPredicated() {
        this.capabilities = new IdentityHashMap<>();
    }

    public <T> CapabilityBrokerDirPredicated with(Capability<T> capability, T aspect, BiPredicate<T, EnumFacing> condition) {
        capabilities.computeIfAbsent(capability, k -> new ArrayList<>()).add(IPair.of(condition, aspect));
        return this;
    }

    public <T> CapabilityBrokerDirPredicated with(Capability<T> capability, T aspect) {
        return with(capability, aspect, (a, f) -> true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        Collection<IPair<BiPredicate, ?>> impls = capabilities.get(capability);
        return impls != null && impls.stream().anyMatch(entry -> entry.getA().test(entry.getB(), facing));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        Collection<IPair<BiPredicate, ?>> impls = capabilities.get(capability);
        return impls == null ? null : impls.stream()
                .filter(entry -> entry.getA().test(entry.getB(), facing))
                .findFirst()
                .map(entry -> (T)entry.getB())
                .orElse(null);
    }

}
