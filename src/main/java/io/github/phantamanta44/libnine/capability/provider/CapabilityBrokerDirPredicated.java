package io.github.phantamanta44.libnine.capability.provider;

import io.github.phantamanta44.libnine.util.tuple.ITriple;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

public class CapabilityBrokerDirPredicated implements ICapabilityProvider {

    private final List<ITriple<Capability, BiPredicate, Object>> capabilities;

    public CapabilityBrokerDirPredicated() {
        this.capabilities = new LinkedList<>();
    }

    public <T> CapabilityBrokerDirPredicated with(Capability<T> capability, T aspect, BiPredicate<T, EnumFacing> condition) {
        capabilities.add(ITriple.of(capability, condition, aspect));
        return this;
    }

    public <T> CapabilityBrokerDirPredicated with(Capability<T> capability, T aspect) {
        return with(capability, aspect, (a, f) -> true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        for (ITriple<Capability, BiPredicate, Object> cap : capabilities) {
            if (cap.getA() == capability && cap.getB().test(cap.getC(), facing)) return true;
        }
        return false;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        for (ITriple<Capability, BiPredicate, Object> cap : capabilities) {
            if (cap.getA() == capability && cap.getB().test(cap.getC(), facing)) return (T)cap.getC();
        }
        return null;
    }

}
