package io.github.phantamanta44.libnine.client.gui.component;

import io.github.phantamanta44.libnine.util.render.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public abstract class GuiComponent {

    protected final int x, y, width, height;
    protected GuiScreen gui;

    public GuiComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    void setGui(GuiScreen gui) {
        this.gui = gui;
    }

    protected void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    protected void drawString(String string, int x, int y, int colour, boolean shadow) {
        Minecraft.getMinecraft().fontRenderer.drawString(string, x, y, colour, shadow);
    }

    protected void drawString(String string, int x, int y, int colour) {
        drawString(string, x, y, colour, false);
    }

    protected void drawTooltip(String string, int x, int y) {
        gui.drawHoveringText(string, x, y);
        RenderHelper.enableGUIStandardItemLighting();
    }

    protected void drawTooltip(List<String> lines, int x, int y) {
        gui.drawHoveringText(lines, x, y);
        RenderHelper.enableGUIStandardItemLighting();
    }

    protected boolean isMouseOver(int mX, int mY) {
        return GuiUtils.isMouseOver(x, y, width, height, mX, mY);
    }

    public abstract void render(float partialTicks, int mX, int mY, boolean mouseOver);

    public void renderTooltip(float partialTicks, int mX, int mY) {
        // NO-OP
    }

    public boolean onClick(int mX, int mY, int button, boolean mouseOver) {
        return false;
    }

    public boolean onDrag(int mX, int mY, int button, long dragTime, boolean mouseOver) {
        return false;
    }

    public boolean onKeyPress(int keyCode, char typed) {
        return false;
    }

    protected static void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler()
                .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

}
