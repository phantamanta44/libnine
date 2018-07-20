package io.github.phantamanta44.libnine.component.reservoir;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class FluidReservoir extends DelegatedIntReservoir implements IFluidTankProperties {

    private final boolean locked;
    private final Collection<BiConsumer<Fluid, Fluid>> callbacks;

    @Nullable
    private Fluid fluid;

    public FluidReservoir(Fluid fluid, IIntReservoir backing, boolean locked) {
        super(backing);
        this.fluid = fluid;
        this.locked = locked;
        this.callbacks = new LinkedList<>();
    }

    public FluidReservoir(Fluid fluid, IIntReservoir backing) {
        super(backing);
        this.fluid = fluid;
        this.locked = true;
        this.callbacks = new LinkedList<>();
    }

    public FluidReservoir(IIntReservoir backing) {
        super(backing);
        this.fluid = null;
        this.locked = false;
        this.callbacks = new LinkedList<>();
    }

    public boolean hasFluid() {
        return fluid != null && getQuantity() > 0;
    }

    @Nullable
    public Fluid getFluid() {
        return fluid;
    }

    public void setFluid(@Nullable Fluid fluid) {
        if (locked) throw new UnsupportedOperationException("Fluid type is locked!");
        Fluid oldFluid = this.fluid;
        this.fluid = fluid;
        callbacks.forEach(c -> c.accept(oldFluid, this.fluid));
    }

    @Override
    public void setQuantity(int qty) {
        super.setQuantity(qty);
        if (qty == 0 && !locked) fluid = null;
    }

    @Nullable
    @Override
    public FluidStack getContents() {
        return fluid != null ? new FluidStack(fluid, getQuantity()) : null;
    }

    @Override
    public boolean canFill() {
        return true;
    }

    @Override
    public boolean canDrain() {
        return true;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill() && (fluid == null || fluid == fluidStack.getFluid());
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain() && fluid != null && fluid == fluidStack.getFluid();
    }

    public void onFluidChange(BiConsumer<Fluid, Fluid> callback) {
        callbacks.add(callback);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        super.serNBT(tag);
        if (!locked) {
            if (fluid == null) {
                tag.setBoolean("NoFluid", true);
            } else {
                tag.setString("Fluid", fluid.getName());
            }
        }
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        super.deserNBT(tag);
        if (!locked) {
            fluid = tag.hasKey("NoFluid") ? null : FluidRegistry.getFluid(tag.getString("Fluid"));
        }
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        super.serBytes(data);
        if (!locked) {
            if (fluid == null) {
                data.writeByte((byte)0);
            } else {
                data.writeFluid(fluid);
            }
        }
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        super.deserBytes(data);
        if (!locked) {
            if (data.readByte() != 0) {
                data.backUp(1);
                setFluid(data.readFluid());
            } else {
                setFluid(null);
            }
        }
    }

}
