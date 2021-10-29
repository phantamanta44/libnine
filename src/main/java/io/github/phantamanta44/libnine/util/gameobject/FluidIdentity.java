package io.github.phantamanta44.libnine.util.gameobject;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;

public class FluidIdentity {

    public static final FluidIdentity EMPTY = new FluidIdentity((String)null);

    public static FluidIdentity getForStack(@Nullable FluidStack stack) {
        return (stack == null || stack.amount <= 0) ? EMPTY : new FluidIdentity(stack.getFluid(), stack.tag);
    }

    @Nullable
    private final String fluidName;
    @Nullable
    private final NBTTagCompound dataTag;

    public FluidIdentity(@Nullable String fluidName, @Nullable NBTTagCompound dataTag) {
        this.fluidName = fluidName;
        this.dataTag = dataTag;
    }

    public FluidIdentity(@Nullable Fluid fluid, @Nullable NBTTagCompound dataTag) {
        this(fluid != null ? fluid.getName() : null, dataTag);
    }

    public FluidIdentity(@Nullable String fluidName) {
        this(fluidName, null);
    }

    public FluidIdentity(@Nullable Fluid fluid) {
        this(fluid != null ? fluid.getName() : null);
    }

    @Nullable
    public String getFluidName() {
        return fluidName;
    }

    @Nullable
    public Fluid getFluid() {
        return fluidName != null ? FluidRegistry.getFluid(fluidName) : null;
    }

    @Nullable
    public NBTTagCompound getDataTag() {
        return dataTag != null ? dataTag.copy() : null;
    }

    public boolean isEmpty() {
        return fluidName == null;
    }

    @Nullable
    public FluidStack createStack(int amount) {
        if (amount <= 0 || fluidName == null) {
            return null;
        }
        return new FluidStack(Objects.requireNonNull(getFluid()), amount, dataTag);
    }

    public boolean matches(@Nullable FluidStack stack) {
        if (fluidName == null) {
            return stack == null || stack.amount <= 0;
        } else if (stack == null || stack.amount <= 0) {
            return false;
        }
        return stack.getFluid().getName().equals(fluidName) && Objects.equals(dataTag, stack.tag);
    }

    @Override
    public int hashCode() {
        if (fluidName == null) {
            return 0;
        }
        return (fluidName.hashCode() * 523) ^ (dataTag == null ? 0xDEADBEEF : dataTag.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FluidIdentity)) {
            return false;
        }
        FluidIdentity o = (FluidIdentity)obj;
        if (fluidName == null) {
            return o.fluidName == null;
        }
        return fluidName.equals(o.fluidName) && Objects.equals(dataTag, o.dataTag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fluidName);
        if (dataTag != null) {
            sb.append(" ").append(dataTag);
        }
        return sb.toString();
    }

}
