package io.github.phantamanta44.libnine.client.gui.component;

import io.github.phantamanta44.libnine.util.render.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public abstract class GuiComponent {

    protected final int x, y, width, height;
    @SuppressWarnings("NotNullFieldNotInitialized")
    protected GuiScreen gui;
    @Nullable
    private GuiComponent nextComponentOnEnter;
    @Nullable
    private GuiComponent nextComponentOnTab;

    public GuiComponent(int x, int y, int width, int height
            , @Nullable GuiComponent nextComponentOnEnter
            , @Nullable GuiComponent nextComponentOnTab
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.nextComponentOnEnter = nextComponentOnEnter;
        this.nextComponentOnTab = nextComponentOnTab;
    }

    public GuiComponent(int x, int y, int width, int height) {
        this(x, y, width, height, null, null);
    }

    public void setGui(GuiScreen gui) {
        this.gui = gui;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Nullable
    public GuiComponent getNextComponentOnEnter() {
        return nextComponentOnEnter;
    }

    public void setNextComponentOnEnter(@Nullable GuiComponent nextComponentOnEnter) {
        this.nextComponentOnEnter = nextComponentOnEnter;
    }

    @Nullable
    public GuiComponent getNextComponentOnTab() {
        return nextComponentOnTab;
    }

    public void setNextComponentOnTab(@Nullable GuiComponent nextComponentOnTab) {
        this.nextComponentOnTab = nextComponentOnTab;
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
