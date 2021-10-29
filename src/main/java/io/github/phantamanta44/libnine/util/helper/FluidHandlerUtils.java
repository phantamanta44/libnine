package io.github.phantamanta44.libnine.util.helper;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class FluidHandlerUtils {

    public static boolean canFluidsStack(@Nullable FluidStack a, @Nullable FluidStack b) {
        if (a == null || a.amount <= 0) {
            return b == null || b.amount <= 0;
        } else if (b == null || b.amount <= 0) {
            return false;
        }
        return a.getFluid().getName().equals(b.getFluid().getName()) && Objects.equals(a.tag, b.tag);
    }

    @Nullable
    public static FluidStack copyStackWithAmount(@Nullable FluidStack base, int amount) {
        if (base == null || amount <= 0) {
            return null;
        }
        FluidStack newStack = base.copy();
        newStack.amount = amount;
        return newStack;
    }

    public static IFluidHandler restrict(IFluidHandler fluidHandler, boolean allowInsert, boolean allowExtract) {
        return new RestrictedFluidHandler(fluidHandler, allowInsert, allowExtract);
    }

    @Deprecated
    public static IFluidHandler insertOnly(IFluidHandler fluidHandler) {
        return restrict(fluidHandler, true, false);
    }

    @Deprecated
    public static IFluidHandler extractOnly(IFluidHandler fluidHandler) {
        return restrict(fluidHandler, false, true);
    }

    public static IFluidTank restrict(IFluidTank tank, boolean allowInsert, boolean allowExtract) {
        return new RestrictedFluidTank(tank, allowInsert, allowExtract);
    }

    @Deprecated
    public static IFluidTank insertOnly(IFluidTank tank) {
        return restrict(tank, true, false);
    }

    @Deprecated
    public static IFluidTank extractOnly(IFluidTank tank) {
        return restrict(tank, false, true);
    }

    public static IFluidHandler asFluidHandler(IFluidTank tank, boolean canFill, boolean canDrain) {
        return new TankFluidHandler(tank, canFill, canDrain);
    }

    public static IFluidHandler asFluidHandler(IFluidTank tank) {
        return new TankFluidHandler(tank, true, true);
    }

    private static class RestrictedFluidHandler implements IFluidHandler {

        private final IFluidHandler delegate;
        private final boolean allowInsert, allowExtract;

        private RestrictedFluidHandler(IFluidHandler delegate, boolean allowInsert, boolean allowExtract) {
            this.delegate = delegate;
            this.allowInsert = allowInsert;
            this.allowExtract = allowExtract;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return Arrays.stream(delegate.getTankProperties())
                    .map(TankPropertiesWrapper::new)
                    .toArray(IFluidTankProperties[]::new);
        }

        @Override
        public int fill(FluidStack fluid, boolean simulate) {
            return allowInsert ? delegate.fill(fluid, simulate) : 0;
        }

        @Override
        @Nullable
        public FluidStack drain(FluidStack fluid, boolean simulate) {
            return allowExtract ? delegate.drain(fluid, simulate) : null;
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean simulate) {
            return allowExtract ? delegate.drain(maxDrain, simulate) : null;
        }

        private class TankPropertiesWrapper implements IFluidTankProperties {

            private final IFluidTankProperties delegateProps;

            private TankPropertiesWrapper(IFluidTankProperties delegateProps) {
                this.delegateProps = delegateProps;
            }

            @Override
            @Nullable
            public FluidStack getContents() {
                return delegateProps.getContents();
            }

            @Override
            public int getCapacity() {
                return delegateProps.getCapacity();
            }

            @Override
            public boolean canFill() {
                return allowInsert && delegateProps.canFill();
            }

            @Override
            public boolean canDrain() {
                return allowExtract && delegateProps.canDrain();
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return allowInsert && delegateProps.canFillFluidType(fluidStack);
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return allowExtract && delegateProps.canDrainFluidType(fluidStack);
            }

        }

    }

    private static class RestrictedFluidTank implements IFluidTank {

        private final IFluidTank delegate;
        private final boolean allowInsert, allowExtract;

        private RestrictedFluidTank(IFluidTank delegate, boolean allowInsert, boolean allowExtract) {
            this.delegate = delegate;
            this.allowInsert = allowInsert;
            this.allowExtract = allowExtract;
        }

        @Override
        @Nullable
        public FluidStack getFluid() {
            return delegate.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return delegate.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return delegate.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return delegate.getInfo();
        }

        @Override
        public int fill(FluidStack fluid, boolean simulate) {
            return allowInsert ? delegate.fill(fluid, simulate) : 0;
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean simulate) {
            return allowExtract ? delegate.drain(maxDrain, simulate) : null;
        }

    }

    private static class TankFluidHandler implements IFluidHandler {

        private final IFluidTank tank;
        private final boolean canFill, canDrain;

        public TankFluidHandler(IFluidTank tank, boolean canFill, boolean canDrain) {
            this.tank = tank;
            this.canFill = canFill;
            this.canDrain = canDrain;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] {
                    new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain)
            };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return canFill ? tank.fill(resource, doFill) : 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (!canDrain || resource.amount <= 0 || !canFluidsStack(resource, tank.getFluid())) {
                return null;
            }
            return tank.drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return canDrain ? tank.drain(maxDrain, doDrain) : null;
        }

    }

}
