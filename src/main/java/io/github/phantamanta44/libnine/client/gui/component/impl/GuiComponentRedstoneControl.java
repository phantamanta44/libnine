package io.github.phantamanta44.libnine.client.gui.component.impl;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.util.render.TextureRegion;
import io.github.phantamanta44.libnine.util.world.IRedstoneControllable;

public class GuiComponentRedstoneControl extends GuiComponent {

    private final TextureRegion bg, bgHover;
    private final TextureRegion ignored, direct, inverted;
    private final int innerX, innerY;
    private final String descIgnored, descDirect, descInverted;
    private final IRedstoneControllable target;

    public GuiComponentRedstoneControl(int x, int y,
                                       TextureRegion bg, TextureRegion bgHover,
                                       TextureRegion ignored, TextureRegion direct, TextureRegion inverted,
                                       int offsetX, int offsetY,
                                       String descIgnored, String descDirect, String descInverted,
                                       IRedstoneControllable target) {
        super(x, y, bg.getWidth(), bg.getHeight());
        this.bg = bg;
        this.bgHover = bgHover;
        this.ignored = ignored;
        this.direct = direct;
        this.inverted = inverted;
        this.innerX = x + offsetX;
        this.innerY = y + offsetY;
        this.descIgnored = descIgnored;
        this.descDirect = descDirect;
        this.descInverted = descInverted;
        this.target = target;
    }
    
    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        (mouseOver ? bgHover : bg).draw(x, y, width, height);
        switch (target.getRedstoneBehaviour()) {
            case IGNORED:
                ignored.draw(innerX, innerY, width, height);
                break;
            case DIRECT:
                direct.draw(innerX, innerY, width, height);
                break;
            case INVERTED:
                inverted.draw(innerX, innerY, width, height);
                break;
        }
    }

    @Override
    public void renderTooltip(float partialTicks, int mX, int mY) {
        switch (target.getRedstoneBehaviour()) {
            case IGNORED:
                drawTooltip(descIgnored, mX, mY);
                break;
            case DIRECT:
                drawTooltip(descDirect, mX, mY);
                break;
            case INVERTED:
                drawTooltip(descInverted, mX, mY);
                break;
        }
    }

    @Override
    public boolean onClick(int mX, int mY, int button, boolean mouseOver) {
        return mouseOver;
    }

}
