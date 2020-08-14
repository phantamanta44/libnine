package io.github.phantamanta44.libnine.client.gui.component.impl;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.constant.NameConst;
import io.github.phantamanta44.libnine.util.render.FluidRenderUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Arrays;
import java.util.function.Supplier;

public class GuiComponentFluidTank extends GuiComponent {

    private final Supplier<FluidTankInfo> tankGetter;

    public GuiComponentFluidTank(int x, int y, int width, int height, Supplier<FluidTankInfo> tankGetter) {
        super(x, y, width, height);
        this.tankGetter = tankGetter;
    }

    public GuiComponentFluidTank(int x, int y, int width, int height, IFluidTank tank) {
        this(x, y, width, height, tank::getInfo);
    }

    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        FluidTankInfo tank = tankGetter.get();
        if (tank.fluid != null && tank.fluid.amount > 0) {
            FluidRenderUtils.renderFluidIntoGuiCleanly(x, y, 16, 16, tank.fluid, tank.capacity);
        }
    }

    @Override
    public void renderTooltip(float partialTicks, int mX, int mY) {
        FluidTankInfo tank = tankGetter.get();
        if (tank.fluid != null && tank.fluid.amount > 0) {
            drawTooltip(Arrays.asList(
                    tank.fluid.getLocalizedName(),
                    TextFormatting.GRAY + String.format("%,d / %,d mB", tank.fluid.amount, tank.capacity)),
                    mX, mY);
        } else {
            drawTooltip(Arrays.asList(
                    I18n.format(NameConst.INFO_EMPTY),
                    TextFormatting.GRAY + String.format("0 / %,d mB", tank.capacity)),
                    mX, mY);
        }
    }

}
