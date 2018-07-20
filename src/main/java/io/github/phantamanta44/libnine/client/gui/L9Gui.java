package io.github.phantamanta44.libnine.client.gui;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.client.gui.component.GuiComponentManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class L9Gui extends GuiScreen implements IScreenDrawable {

    protected final int sizeX;
    protected final int sizeY;

    @Nullable
    private final ResourceLocation bg;
    private final GuiComponentManager components;

    private int posX;
    private int posY;

    public L9Gui(@Nullable ResourceLocation bg, int sizeX, int sizeY) {
        this.bg = bg;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.components = new GuiComponentManager(this);
    }

    public L9Gui(@Nullable ResourceLocation bg) {
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
    }

    @Override
    public void addComponent(GuiComponent comp) {
        components.register(comp);
    }

    @Override
    public void drawScreen(int mX, int mY, float partialTicks) {
        drawBackground(partialTicks, mX - posX, mY - posY);
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
        drawForeground(partialTicks, mX - posX, mY - posY);
        components.draw(partialTicks, mX - posX, mY - posY);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
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
        // NO-OP
    }

    protected void drawString(String string, int x, int y, int colour, boolean shadow) {
        Minecraft.getMinecraft().fontRenderer.drawString(string, x, y, colour, shadow);
    }

    protected void drawString(String string, int x, int y, int colour) {
        drawString(string, x, y, colour, false);
    }

    protected void drawTooltip(String string, int x, int y) {
        drawHoveringText(string, x, y);
        RenderHelper.enableGUIStandardItemLighting();
    }

    protected void drawTooltip(List<String> lines, int x, int y) {
        drawHoveringText(lines, x, y);
        RenderHelper.enableGUIStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mX, int mY, int button) throws IOException {
        if (components.handleMouseClick(mX - posX, mY - posY, button)) super.mouseClicked(mX, mY, button);
    }

    @Override
    protected void mouseClickMove(int mX, int mY, int button, long dragTime) {
        if (components.handleMouseDrag(mX - posX, mY - posY, button, dragTime)) {
            super.mouseClickMove(mX, mY, button, dragTime);
        }
    }

    @Override
    protected void keyTyped(char typed, int keyCode) throws IOException {
        if (components.handleKeyTyped(typed, keyCode)) super.keyTyped(typed, keyCode);
    }
    
}
