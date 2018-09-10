package io.github.phantamanta44.libnine.client.gui.component.impl;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.util.function.IFloatSupplier;
import io.github.phantamanta44.libnine.util.render.TextureRegion;

import java.util.function.Supplier;

public class GuiComponentVerticalBar extends GuiComponent {

    private final TextureRegion bg, fg;
    private final int innerX, innerY, innerWidth, innerHeight;
    private final IFloatSupplier dataSrc;
    private final Supplier<String> ttSrc;

    public GuiComponentVerticalBar(int x, int y,
                                   TextureRegion bg, TextureRegion fg, int offsetX, int offsetY,
                                   IFloatSupplier dataSrc, Supplier<String> ttSrc) {
        super(x, y, bg.getWidth(), bg.getHeight());
        this.bg = bg;
        this.fg = fg;
        this.innerX = x + offsetX;
        this.innerY = y + offsetY;
        this.innerWidth = width - 2 * offsetX;
        this.innerHeight = height - 2 * offsetY;
        this.dataSrc = dataSrc;
        this.ttSrc = ttSrc;
    }

    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        bg.draw(x, y, width, height);
        fg.drawPartial(innerX, innerY, innerWidth, innerHeight, 0, 1 - dataSrc.get(), 1, 1);
    }

    @Override
    public void renderTooltip(float partialTicks, int mX, int mY) {
        drawTooltip(ttSrc.get(), mX, mY);
    }

}
