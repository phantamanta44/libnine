package io.github.phantamanta44.libnine.client.gui.component;

import io.github.phantamanta44.libnine.client.gui.component.impl.GuiComponentTextInput;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                break;
            }
        }
        if (passThrough) {
            passThrough = focusFirstTextFieldOnKeys(Arrays.asList(Keyboard.KEY_TAB, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN), keyCode);
        }
        return passThrough;
    }

    private boolean focusFirstTextFieldOnKeys(List<Integer> keys, int keyCode) {
        if (!keys.contains(keyCode)) return true;
        for (ComponentState componentState: comps) {
            if (componentState.getComp() instanceof GuiComponentTextInput) {
                ((GuiComponentTextInput) componentState.getComp()).setFocused(true);
                return false;
            }
        }
        return true;
    }

    private static class ComponentState {

        private final GuiComponent comp;
        private boolean mouseOver;

        ComponentState(GuiComponent comp) {
            this.comp = comp;
            this.mouseOver = false;
        }

        public GuiComponent getComp() {
            return comp;
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
            boolean success = comp.onKeyPress(keyCode, typed);
            if (keyCode == Keyboard.KEY_ESCAPE
                    || keyCode == Keyboard.KEY_RETURN
                    || keyCode == Keyboard.KEY_NUMPADENTER
                    || keyCode == Keyboard.KEY_TAB)
                changeFocus(comp, keyCode);

            return success;
        }

        // for now only text components. Possibly buttons and such later?
        // that would enable keyboard control over buttons, with tab/enter to select
        void changeFocus(GuiComponent comp, int keyCode) {
            if (!(comp instanceof GuiComponentTextInput)) return;
            GuiComponentTextInput component = (GuiComponentTextInput) comp;
            if (!component.isFocused()) return;
            component.setFocused(false);

            GuiComponentTextInput nextComponent = getNextComponent(component, keyCode);
            if (nextComponent != null) {
                nextComponent.setFocused(true);
            }
        }

        @Nullable
        private GuiComponentTextInput getNextComponent(GuiComponentTextInput component, int keyCode) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                GuiComponent next = component.getNextComponentOnEnter();
                if (next instanceof GuiComponentTextInput) {
                    return (GuiComponentTextInput) next;
                }
            } else if (keyCode == Keyboard.KEY_TAB) {
                GuiComponent next = component.getNextComponentOnTab();
                if (next instanceof GuiComponentTextInput) {
                    return (GuiComponentTextInput) next;
                }
            }
            return null;
        }
    }

}
