package io.github.phantamanta44.libnine.client.gui.component.impl;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.util.render.TextureRegion;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;

public class GuiComponentButton extends GuiComponent {

    @Nullable
    private final String tooltipKey;
    private final TextureRegion texNormal, texHovered, texDisabled;
    private final Runnable callback;
    private boolean disabled = false;

    public GuiComponentButton(int x, int y, int width, int height, @Nullable String tooltipKey,
                              TextureRegion texNormal, TextureRegion texHovered, TextureRegion texDisabled,
                              Runnable callback) {
        super(x, y, width, height);
        this.tooltipKey = tooltipKey;
        this.texNormal = texNormal;
        this.texHovered = texHovered;
        this.texDisabled = texDisabled;
        this.callback = callback;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        (disabled ? texDisabled : (mouseOver ? texHovered : texNormal)).draw(x, y, width, height);
    }

    @Override
    public boolean onClick(int mX, int mY, int button, boolean mouseOver) {
        if (mouseOver && !disabled && button == 0) {
            callback.run();
            playClickSound();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void renderTooltip(float partialTicks, int mX, int mY) {
        if (tooltipKey != null) {
            drawTooltip(I18n.format(tooltipKey), mX, mY);
        }
    }

}
