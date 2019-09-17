package io.github.phantamanta44.libnine.client.gui.component;

import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

public class GuiComponentManager {

    private final GuiScreen gui;
    private final ArrayList<ComponentState> comps;

    public GuiComponentManager(GuiScreen gui) {
        this.gui = gui;
        this.comps = new ArrayList<>();
    }

    public void register(GuiComponent comp) {
        comp.setGui(gui);
        comps.add(new ComponentState(comp));
    }

    public void draw(float partialTicks, int mX, int mY) {
        comps.forEach(c -> c.drawAndUpdate(partialTicks, mX, mY));
        comps.forEach(c -> c.drawTooltip(partialTicks, mX, mY));
    }

    public boolean handleMouseClick(int mX, int mY, int button) {
        boolean passThrough = true;
        for (ComponentState comp : comps) {
            if (comp.handleMouseClick(mX, mY, button)) {
                passThrough = false;
            }
        }
        return passThrough;
    }

    public boolean handleMouseDrag(int mX, int mY, int button, long dragTime) {
        boolean passThrough = true;
        for (ComponentState comp : comps) {
            if (comp.handleMouseDrag(mX, mY, button, dragTime)) {
                passThrough = false;
            }
        }
        return passThrough;
    }

    public boolean handleKeyTyped(char typed, int keyCode) {
        boolean passThrough = true;
        for (ComponentState comp : comps) {
            if (comp.handleKeyTyped(typed, keyCode)) {
                passThrough = false;
            }
        }
        return passThrough;
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
