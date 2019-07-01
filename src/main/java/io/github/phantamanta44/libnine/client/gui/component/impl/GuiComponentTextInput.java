package io.github.phantamanta44.libnine.client.gui.component.impl;

import io.github.phantamanta44.libnine.client.gui.component.GuiComponent;
import io.github.phantamanta44.libnine.util.helper.InputUtils;
import io.github.phantamanta44.libnine.util.render.GuiUtils;
import io.github.phantamanta44.libnine.util.render.TextureRegion;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GuiComponentTextInput extends GuiComponent {

    private final int boxLength, textLength;
    private final TextureRegion btnTex, btnTexHover, btnTextDisabled;
    private final int validColour, invalidColour;
    private final Predicate<String> validator;
    private final Consumer<String> callback;
    private boolean focused, valid;
    private String value;
    @Nullable
    private final String tooltipKey;

    public GuiComponentTextInput(int x, int y, int boxLength, int textLength,
                                 TextureRegion btnTex, TextureRegion btnTexHover, TextureRegion btnTexDisabled,
                                 int validColour, int invalidColour,
                                 Predicate<String> validator, Consumer<String> callback,
                                 String initialValue, @Nullable String tooltipKey) {
        super(x, y, boxLength + 9 + GuiUtils.getFontHeight(), GuiUtils.getFontHeight() + 4);
        this.boxLength = boxLength;
        this.textLength = textLength;
        this.btnTex = btnTex;
        this.btnTexHover = btnTexHover;
        this.btnTextDisabled = btnTexDisabled;
        this.validColour = validColour;
        this.invalidColour = invalidColour;
        this.validator = validator;
        this.callback = callback;
        this.focused = false;
        this.value = initialValue;
        this.tooltipKey = tooltipKey;
        updateValidity();
    }

    public GuiComponentTextInput(int x, int y, int boxLength, int textLength,
                                 TextureRegion btnTex, TextureRegion btnTexHover, TextureRegion btnTexDisabled,
                                 int validColour, int invalidColour,
                                 Predicate<String> validator, Consumer<String> callback, String initial) {
        this(x, y, boxLength, textLength,
                btnTex, btnTexHover, btnTexDisabled, validColour, invalidColour, validator, callback, initial, null);
    }

    public GuiComponentTextInput(int x, int y, int boxLength, int textLength,
                                 TextureRegion btnTex, TextureRegion btnTexHover, TextureRegion btnTexDisabled,
                                 int validColour, int invalidColour,
                                 Predicate<String> validator, Consumer<String> callback) {
        this(x, y, boxLength, textLength,
                btnTex, btnTexHover, btnTexDisabled, validColour, invalidColour, validator, callback, "");
    }

    public void setValue(String value) {
        this.value = value.substring(0, Math.min(value.length(), textLength));
        updateValidity();
    }

    private void updateValidity() {
        valid = validator.test(value);
    }

    private boolean isMouseOverButton(int mX, int mY) {
        return GuiUtils.isMouseOver(x + boxLength + 5, y, GuiUtils.getFontHeight() + 4, GuiUtils.getFontHeight() + 4, mX, mY);
    }

    private boolean isMouseOverBox(int mX, int mY) {
        return GuiUtils.isMouseOver(x, y, boxLength + 4, GuiUtils.getFontHeight() + 4, mX, mY);
    }

    public boolean isFocused() {
        return focused;
    }

    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        (valid ? (isMouseOverButton(mX, mY) ? btnTexHover : btnTex) : btnTextDisabled).draw(x + boxLength + 5, y, 13, 13);
        int colour = valid ? validColour : invalidColour;
        drawString(value, x + 2, y + 3, colour);
        if (focused && (System.currentTimeMillis() % 1000) < 500) {
            GuiUtils.drawRect(x + GuiUtils.getStringWidth(value) + 2, y + 2, 1, GuiUtils.getFontHeight(), colour | 0xFF000000);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void renderTooltip(float partialTicks, int mX, int mY) {
        if (tooltipKey != null && isMouseOverBox(mX, mY)) {
            drawTooltip(I18n.format(tooltipKey), mX, mY);
        }
    }

    @Override
    public boolean onClick(int mX, int mY, int button, boolean mouseOver) {
        if (mouseOver) {
            if (isMouseOverBox(mX, mY)) {
                if (button == 1) value = "";
                focused = true;
            } else {
                focused = false;
                if (isMouseOverButton(mX, mY) && valid) {
                    playClickSound();
                    callback.accept(value);
                }
            }
            return true;
        } else {
            focused = false;
            return false;
        }
    }

    @Override
    public boolean onKeyPress(int keyCode, char typed) {
        if (focused) {
            int currentLength = value.length();
            if (currentLength < textLength && typed >= 32 && typed < 127) {
                value += typed;
                updateValidity();
            } else if (keyCode == Keyboard.KEY_BACK && !value.isEmpty()) {
                if (InputUtils.checkModsNonExclusive(InputUtils.ModKey.CTRL)) {
                    int endIndex = 0;
                    if (Character.isLetterOrDigit(value.charAt(currentLength - 1))) {
                        for (int i = currentLength - 2; i >= 0; i--) {
                            if (!Character.isLetterOrDigit(value.charAt(i))) {
                                endIndex = i;
                                break;
                            }
                        }
                    } else {
                        for (int i = currentLength - 2; i >= 0; i--) {
                            if (Character.isLetterOrDigit(value.charAt(i))) {
                                endIndex = i;
                                break;
                            }
                        }
                    }
                    value = value.substring(0, endIndex + 1);
                } else {
                    value = value.substring(0, currentLength - 1);
                }
                updateValidity();
            } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                updateValidity();
                if (valid) callback.accept(value);
            } else if (keyCode == Keyboard.KEY_ESCAPE) {
                focused = false;
            }
            return true;
        }
        return false;
    }

}
