package io.github.phantamanta44.libnine.client.gui.component;

import net.minecraft.client.gui.GuiScreen;

import java.util.LinkedList;

public class GuiComponentManager {

    private final GuiScreen gui;
    private final LinkedList<ComponentState> comps;

    private int mouseOffsetX;
    private int mouseOffsetY;

    public GuiComponentManager(GuiScreen gui) {
        this.gui = gui;
        this.comps = new LinkedList<>();
        this.mouseOffsetX = this.mouseOffsetY = 0;
    }

    public void register(GuiComponent comp) {
        comp.setGui(gui);
        comps.add(new ComponentState(comp));
    }

    public void setMouseOffset(int mouseOffsetX, int mouseOffsetY) {
        this.mouseOffsetX = mouseOffsetX;
        this.mouseOffsetY = mouseOffsetY;
    }

    public void draw(float partialTicks, int mXRaw, int mYRaw) {
        int mX = mXRaw - mouseOffsetX, mY = mYRaw - mouseOffsetY;
        comps.forEach(c -> c.drawAndUpdate(partialTicks, mX, mY));
        comps.forEach(c -> c.drawTooltip(partialTicks, mX, mY));
    }

    public boolean handleMouseClick(int mXRaw, int mYRaw, int button) {
        int mX = mXRaw - mouseOffsetX, mY = mYRaw - mouseOffsetY;
        return comps.stream().noneMatch(c -> c.handleMouseClick(mX, mY, button));
    }

    public boolean handleMouseDrag(int mXRaw, int mYRaw, int button, long dragTime) {
        int mX = mXRaw - mouseOffsetX, mY = mYRaw - mouseOffsetY;
        return comps.stream().noneMatch(c -> c.handleMouseDrag(mX, mY, button, dragTime));
    }

    public boolean handleKeyTyped(char typed, int keyCode) {
        return comps.stream().noneMatch(c -> c.handleKeyTyped(typed, keyCode));
    }

    private static class ComponentState {

        private final GuiComponent comp;
        private boolean mouseOver;

        ComponentState(GuiComponent comp) {
            this.comp = comp;
            this.mouseOver = false;
        }

        void drawAndUpdate(float partialTicks, int mX, int mY) {
            mouseOver = comp.isMouseOver(mX, mY);
            comp.render(partialTicks, mX, mY, mouseOver);
        }
        
        void drawTooltip(float partialTicks, int mX, int mY) {
            if (mouseOver) comp.renderTooltip(partialTicks, mX, mY);
        }

        boolean handleMouseClick(int mX, int mY, int button) {
            return comp.onClick(mX, mY, button, mouseOver);
        }

        boolean handleMouseDrag(int mX, int mY, int button, long dragTime) {
            return comp.onDrag(mX, mY, button, dragTime, mouseOver);
        }

        boolean handleKeyTyped(char typed, int keyCode) {
            return comp.onKeyPress(keyCode, typed);
        }
        
    }

}
