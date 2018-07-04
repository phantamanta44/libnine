package io.github.phantamanta44.libnine.client.gui;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.client.gui.component.GuiComponentManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class L9Gui extends GuiScreen implements IScreenDrawable {

    protected final int sizeX;
    protected final int sizeY;

    private final ResourceLocation bg;
    private final GuiComponentManager components;

    private int posX;
    private int posY;

    public L9Gui(ResourceLocation bg, int sizeX, int sizeY) {
        this.bg = bg;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.components = new GuiComponentManager(this);
    }

    public L9Gui(ResourceLocation bg) {
        this(bg, 176, 166);
    }

    public L9Gui() {
        this(null);
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
        drawBackground(partialTicks, mX, mY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        super.drawScreen(mX, mY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0.0F);
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        drawForeground(partialTicks, mX, mY);
        components.draw(partialTicks, mX, mY);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void drawBackground(float partialTicks, int mX, int mY) {
        if (bg != null) {
            mc.renderEngine.bindTexture(bg);
            drawTexturedModalRect(posX, posY, 0, 0, sizeX, sizeY);
        }
    }

    @Override
    public void drawForeground(float partialTicks, int mX, int mY) {
        // NO-OP
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