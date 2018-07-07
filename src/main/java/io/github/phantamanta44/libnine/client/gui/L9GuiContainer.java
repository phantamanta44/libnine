package io.github.phantamanta44.libnine.client.gui;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.client.gui.component.GuiComponentManager;
import io.github.phantamanta44.libnine.gui.L9Container;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import java.io.IOException;

public class L9GuiContainer extends GuiContainer implements IScreenDrawable {

    protected final int sizeX;
    protected final int sizeY;

    private final ResourceLocation bg;
    private final GuiComponentManager components;

    private int posX;
    private int posY;
    private float partialTicks;

    public L9GuiContainer(L9Container container, ResourceLocation bg, int sizeX, int sizeY) {
        super(container);
        this.bg = bg;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.components = new GuiComponentManager(this);
    }

    public L9GuiContainer(L9Container container, ResourceLocation bg) {
        this(container, bg, 176, 166);
    }

    public L9GuiContainer(L9Container container) {
        this(container, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        posX = (this.width - this.sizeX) / 2;
        posY = (this.height - this.sizeY) / 2;
        components.setMouseOffset(posX, posY);
    }

    @Override
    public void addComponent(GuiComponent comp) {
        components.register(comp);
    }

    @Override
    public void drawScreen(int mX, int mY, float partialTicks) {
        super.drawScreen(mX, mY, partialTicks);
        renderHoveredToolTip(mX, mY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mX, int mY) {
        this.partialTicks = partialTicks;
        drawBackground(partialTicks, mX, mY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mX, int mY) {
        drawForeground(partialTicks, mX, mY);
        components.draw(partialTicks, mX, mY);
    }

    @Override
    public void drawBackground(float partialTicks, int mX, int mY) {
        drawDefaultBackground();
        if (bg != null) {
            mc.renderEngine.bindTexture(bg);
            drawTexturedModalRect(posX, posY, 0, 0, sizeX, sizeY);
        }
    }

    @Override
    public void drawForeground(float partialTicks, int mX, int mY) {
        drawPlayerInventoryName();
    }

    protected void drawContainerName(String name) {
        fontRenderer.drawString(name, 8, 6, DEF_TEXT_COL);
    }

    protected void drawPlayerInventoryName() {
        fontRenderer.drawString(I18n.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, DEF_TEXT_COL);
    }

    @Override
    protected void mouseClicked(int mX, int mY, int button) throws IOException {
        if (components.handleMouseClick(mX, mY, button)) super.mouseClicked(mX, mY, button);
    }

    @Override
    protected void mouseClickMove(int mX, int mY, int button, long dragTime) {
        if (components.handleMouseDrag(mX, mY, button, dragTime)) {
            super.mouseClickMove(mX, mY, button, dragTime);
        }
    }

    @Override
    protected void keyTyped(char typed, int keyCode) throws IOException {
        if (components.handleKeyTyped(typed, keyCode)) super.keyTyped(typed, keyCode);
    }
    
}
