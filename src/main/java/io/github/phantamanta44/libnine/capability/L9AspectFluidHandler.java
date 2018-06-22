package io.github.phantamanta44.libnine.capability;

import io.github.phantamanta44.libnine.component.FluidReservoir;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.helper.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class L9AspectFluidHandler implements IFluidHandler, ISerializable {

    private final FluidReservoir[] tanks;

    public L9AspectFluidHandler(FluidReservoir mainTank, FluidReservoir... secondaryTanks) {
        this.tanks = new FluidReservoir[secondaryTanks.length + 1];
        tanks[0] = mainTank;
        System.arraycopy(secondaryTanks, 0, tanks, 1, secondaryTanks.length);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return tanks;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        for (FluidReservoir tank : tanks) {
            if (tank.canFillFluidType(resource)) {
                tank.setFluid(resource.getFluid());
                return tank.offer(resource.amount, doFill);
            }
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        for (FluidReservoir tank : tanks) {
            if (tank.canDrainFluidType(resource)) {
                return new FluidStack(tank.getFluid(), tank.draw(resource.amount, doDrain));
            }
        }
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        for (FluidReservoir tank : tanks) {
            if (tank.canDrain()) return new FluidStack(tank.getFluid(), tank.draw(maxDrain, doDrain));
        }
        return null;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (FluidReservoir tank : tanks) {
            NBTTagCompound tankTag = new NBTTagCompound();
            tank.serializeNBT(tankTag);
            list.appendTag(tankTag);
        }
        tag.setTag("Tanks", list);
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tanks.length; i++) tanks[i].deserializeNBT(list.getCompoundTagAt(i));
    }

    @Override
    public void serializeBytes(ByteUtils.Writer data) {
        for (FluidReservoir tank : tanks) tank.serializeBytes(data);
    }

    @Override
    public void deserializeBytes(ByteUtils.Reader data) {
        for (FluidReservoir tank : tanks) tank.deserializeBytes(data);
    }

}
