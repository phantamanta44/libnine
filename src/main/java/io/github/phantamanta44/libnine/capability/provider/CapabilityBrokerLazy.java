package io.github.phantamanta44.libnine.capability.provider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class CapabilityBrokerLazy extends CapabilityBroker {

    private final Function<Capability<?>, Object> factory;
    private final Set<Capability<?>> closedSet;

    public CapabilityBrokerLazy(Function<Capability<?>, Object> factory) {
        this.factory = factory;
        this.closedSet = new HashSet<>();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing)
                || (!closedSet.contains(capability) && tryLateResolve(capability) != null);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T aspect = super.getCapability(capability, facing);
        return aspect != null ? aspect : (closedSet.contains(capability) ? null : tryLateResolve(capability));
    }

    @SuppressWarnings("unchecked")
    private <T> T tryLateResolve(Capability<T> capability) {
        closedSet.add(capability);
        T aspect = (T)factory.apply(capability);
        if (aspect != null) with(capability, aspect);
        return aspect;
    }

}
