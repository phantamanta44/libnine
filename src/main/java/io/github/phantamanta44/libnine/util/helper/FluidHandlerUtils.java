package io.github.phantamanta44.libnine.util.helper;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FluidHandlerUtils {

    public static IFluidHandler insertOnly(IFluidHandler fluidHandler) {
        return new InsertOnlyFluidHandler(fluidHandler);
    }

    public static IFluidHandler extractOnly(IFluidHandler fluidHandler) {
        return new ExtractOnlyFluidHandler(fluidHandler);
    }

    public static IFluidTank insertOnly(IFluidTank tank) {
        return new InsertOnlyFluidTank(tank);
    }

    public static IFluidTank extractOnly(IFluidTank tank) {
        return new ExtractOnlyFluidTank(tank);
    }

    private static class InsertOnlyFluidHandler implements IFluidHandler {

        private final IFluidHandler delegate;

        InsertOnlyFluidHandler(IFluidHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return Arrays.stream(delegate.getTankProperties()).map(Properties::new).toArray(IFluidTankProperties[]::new);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return delegate.fill(resource, doFill);
        }

        @Override
        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        private static class Properties implements IFluidTankProperties {

            private final IFluidTankProperties props;

            Properties(IFluidTankProperties props) {
                this.props = props;
            }

            @Override
            @Nullable
            public FluidStack getContents() {
                return props.getContents();
            }

            @Override
            public int getCapacity() {
                return props.getCapacity();
            }

            @Override
            public boolean canFill() {
                return props.canFill();
            }

            @Override
            public boolean canDrain() {
                return false;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return props.canFillFluidType(fluidStack);
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return false;
            }

        }

    }

    private static class ExtractOnlyFluidHandler implements IFluidHandler {

        private final IFluidHandler delegate;

        ExtractOnlyFluidHandler(IFluidHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return Arrays.stream(delegate.getTankProperties()).map(Properties::new).toArray(IFluidTankProperties[]::new);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return delegate.drain(resource, doDrain);
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return delegate.drain(maxDrain, doDrain);
        }

        private static class Properties implements IFluidTankProperties {

            private final IFluidTankProperties props;

            Properties(IFluidTankProperties props) {
                this.props = props;
            }

            @Override
            @Nullable
            public FluidStack getContents() {
                return props.getContents();
            }

            @Override
            public int getCapacity() {
                return props.getCapacity();
            }

            @Override
            public boolean canFill() {
                return false;
            }

            @Override
            public boolean canDrain() {
                return props.canDrain();
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return false;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return props.canDrainFluidType(fluidStack);
            }

        }

    }

    private static class InsertOnlyFluidTank implements IFluidTank {

        private final IFluidTank delegate;

        InsertOnlyFluidTank(IFluidTank delegate) {
            this.delegate = delegate;
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
        public int fill(FluidStack resource, boolean doFill) {
            return delegate.fill(resource, doFill);
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

    }

    private static class ExtractOnlyFluidTank implements IFluidTank {

        private final IFluidTank delegate;

        ExtractOnlyFluidTank(IFluidTank delegate) {
            this.delegate = delegate;
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
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return delegate.drain(maxDrain, doDrain);
        }

    }

}
