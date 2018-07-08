package io.github.phantamanta44.libnine.capability.impl;

import io.github.phantamanta44.libnine.component.reservoir.FluidReservoir;
import io.github.phantamanta44.libnine.util.data.ISerializable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class L9AspectFluidHandlerItem extends L9AspectFluidHandler implements IFluidHandlerItem, ISerializable {

    private final ItemStack container;

    public L9AspectFluidHandlerItem(ItemStack container, FluidReservoir mainTank, FluidReservoir... secondaryTanks) {
        super(mainTank, secondaryTanks);
        this.container = container;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }

}
