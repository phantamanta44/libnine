package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.component.reservoir.FluidReservoir;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class L9AspectFluidHandler implements IFluidHandler, ISerializable {

    private final boolean overflowProtection;
    private final FluidReservoir[] tanks;

    public L9AspectFluidHandler(boolean overflowProtection, FluidReservoir... tanks) {
        this.overflowProtection = overflowProtection;
        this.tanks = tanks;
    }

    public L9AspectFluidHandler(boolean overflowProtection, int tankCount, Supplier<FluidReservoir> tankFactory) {
        this(overflowProtection, IntStream.range(0, tankCount).mapToObj(i -> tankFactory.get()).toArray(FluidReservoir[]::new));
    }

    public FluidReservoir[] getTanks() {
        return tanks;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        IFluidTankProperties[] props = new IFluidTankProperties[tanks.length];
        for (int i = 0; i < tanks.length; i++) {
            props[i] = new FluidTankProperties(tanks[i].getFluid(), tanks[i].getCapacity());
        }
        return props;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int maxFill = resource.amount;
        resource = resource.copy();
        for (FluidReservoir tank : tanks) {
            if (tank.canFillFluidType(resource)) {
                int amount = tank.fill(resource, doFill);
                if (amount > 0 && overflowProtection) {
                    return amount;
                }
                resource.amount -= amount;
                if (resource.amount <= 0) {
                    return maxFill;
                }
            }
        }
        return maxFill - resource.amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource.amount <= 0) {
            return null;
        }
        int drainRemaining = resource.amount;
        for (FluidReservoir tank : tanks) {
            if (tank.canDrainFluidType(resource) && tank.hasFluid()) {
                FluidStack drained = tank.drain(drainRemaining, doDrain);
                if (drained != null) {
                    drainRemaining -= drained.amount;
                    if (drainRemaining <= 0) {
                        return resource.copy();
                    }
                }
            }
        }
        if (drainRemaining >= resource.amount) {
            return null;
        }
        resource = resource.copy();
        resource.amount -= drainRemaining;
        return resource;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        for (FluidReservoir tank : tanks) {
            if (tank.hasFluid()) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null) {
                    return drained;
                }
            }
        }
        return null;
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (FluidReservoir tank : tanks) {
            NBTTagCompound tankTag = new NBTTagCompound();
            tank.serNBT(tankTag);
            list.appendTag(tankTag);
        }
        tag.setTag("Tanks", list);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tanks.length; i++) tanks[i].deserNBT(list.getCompoundTagAt(i));
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        for (FluidReservoir tank : tanks) tank.serBytes(data);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        for (FluidReservoir tank : tanks) tank.deserBytes(data);
    }

}
