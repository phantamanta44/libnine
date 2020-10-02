package io.github.phantamanta44.libnine.component.reservoir;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

public class FluidReservoir extends DelegatedIntReservoir implements IFluidTank {

    public static FluidReservoir observable(IIntReservoir backing, Runnable observer) {
        FluidReservoir reservoir = new FluidReservoir(backing);
        reservoir.onFluidChange((o, n) -> observer.run());
        reservoir.onQuantityChange((o, n) -> observer.run());
        return reservoir;
    }

    private final boolean locked;
    private final Collection<BiConsumer<FluidStack, FluidStack>> callbacks;

    @Nullable
    private Fluid fluid;
    @Nullable
    private NBTTagCompound fluidData;

    public FluidReservoir(@Nullable Fluid fluid, @Nullable NBTTagCompound fluidData, IIntReservoir backing, boolean locked) {
        super(backing);
        this.fluid = fluid;
        this.fluidData = fluidData;
        this.locked = locked;
        this.callbacks = new ArrayList<>();
    }

    public FluidReservoir(@Nullable FluidStack fluid, IIntReservoir backing, boolean locked) {
        this(fluid != null ? fluid.getFluid() : null, fluid != null ? fluid.tag : null, backing, locked);
    }

    public FluidReservoir(@Nullable Fluid fluid, @Nullable NBTTagCompound fluidData, IIntReservoir backing) {
        this(fluid, fluidData, backing, true);
    }

    public FluidReservoir(@Nullable FluidStack fluid, IIntReservoir backing) {
        this(fluid, backing, true);
    }

    public FluidReservoir(IIntReservoir backing) {
        this(null, null, backing, false);
    }

    public boolean hasFluid() {
        return fluid != null && getQuantity() > 0;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        if (fluid == null) {
            return null;
        }
        int quantity = getQuantity();
        return quantity > 0 ? new FluidStack(fluid, quantity, fluidData) : null;
    }

    @Nullable
    public Fluid getFluidType() {
        return fluid;
    }

    @Nullable
    public NBTTagCompound getFluidData() {
        return fluidData;
    }

    public void setFluid(@Nullable Fluid fluid, @Nullable NBTTagCompound fluidData) {
        if (!Objects.equals(fluid, this.fluid) || !Objects.equals(fluidData, this.fluidData)) {
            if (locked) throw new UnsupportedOperationException("Fluid type is locked!");
            FluidStack oldFluid = getFluid();
            this.fluid = fluid;
            this.fluidData = fluidData;
            for (BiConsumer<FluidStack, FluidStack> callback : callbacks) {
                callback.accept(oldFluid, getFluid());
            }
        }
    }

    public void setFluid(@Nullable Fluid fluid) {
        setFluid(fluid, null);
    }

    @Override
    public void setQuantity(int qty) {
        if (qty < 0) {
            qty = 0;
        }
        if (getQuantity() == qty) {
            return;
        }
        super.setQuantity(qty);
        if (qty == 0 && !locked) {
            FluidStack oldFluid = getFluid();
            fluid = null;
            fluidData = null;
            for (BiConsumer<FluidStack, FluidStack> callback : callbacks) {
                callback.accept(oldFluid, null);
            }
        }
    }

    @Override
    public int getFluidAmount() {
        return getQuantity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource.amount <= 0) {
            return 0;
        } else if (fluid == null) {
            if (doFill) {
                fluid = resource.getFluid();
                fluidData = resource.tag;
                for (BiConsumer<FluidStack, FluidStack> callback : callbacks) {
                    callback.accept(null, getFluid());
                }
            }
        } else if (resource.getFluid() != fluid || !Objects.equals(resource.tag, fluidData)) {
            return 0;
        }
        return offer(resource.amount, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (fluid == null) {
            return null;
        }
        int drained = draw(maxDrain, doDrain);
        return drained > 0 ? new FluidStack(fluid, drained, fluidData) : null;
    }

    public boolean canFillFluidType(FluidStack fluidStack) {
        return fluid == null || (fluid == fluidStack.getFluid() && Objects.equals(fluidData, fluidStack.tag));
    }

    public boolean canDrainFluidType(FluidStack fluidStack) {
        return fluid != null && fluid == fluidStack.getFluid() && Objects.equals(fluidData, fluidStack.tag);
    }

    public void onFluidChange(BiConsumer<FluidStack, FluidStack> callback) {
        callbacks.add(callback);
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        super.serNBT(tag);
        if (!locked) {
            if (fluid != null) {
                tag.setString("Fluid", fluid.getName());
                if (fluidData != null) {
                    tag.setTag("FluidData", fluidData);
                }
            }
        }
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        super.deserNBT(tag);
        if (!locked) {
            if (tag.hasKey("Fluid", Constants.NBT.TAG_STRING)) {
                fluid = FluidRegistry.getFluid(tag.getString("Fluid"));
                fluidData = tag.hasKey("FluidData", Constants.NBT.TAG_COMPOUND)
                        ? tag.getCompoundTag("FluidData") : null;
            }
        }
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        super.serBytes(data);
        if (!locked) {
            if (fluid == null) {
                data.writeByte((byte)0);
            } else {
                data.writeByte((byte)(0x1 | (fluidData != null ? 0x2 : 0))).writeFluid(fluid);
                if (fluidData != null) {
                    data.writeTagCompound(fluidData);
                }
            }
        }
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        super.deserBytes(data);
        if (!locked) {
            byte mask = data.readByte();
            if ((mask & 0x1) != 0) {
                setFluid(data.readFluid(), (mask & 0x2) != 0 ? data.readTagCompound() : null);
            } else {
                setFluid(null);
            }
        }
    }

}
