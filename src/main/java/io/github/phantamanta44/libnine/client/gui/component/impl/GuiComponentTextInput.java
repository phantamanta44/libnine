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
    @Nullable
    private final TextureRegion btnTex, btnTexHover, btnTextDisabled;
    private final int validColour, invalidColour;
    private final Predicate<String> validator;
    private final Consumer<String> callback;
    private boolean focused, valid;
    private String value;
    @Nullable
    private final String tooltipKey;
    private FieldType fieldType = FieldType.ALL_TEXT;


    public GuiComponentTextInput(int x, int y, int boxLength, int textLength
            , @Nullable TextureRegion btnTex
            , @Nullable TextureRegion btnTexHover
            , @Nullable TextureRegion btnTexDisabled
            , int validColour, int invalidColour
            , Predicate<String> validator, Consumer<String> callback
            , String initialValue, @Nullable String tooltipKey
    ) {
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

    public GuiComponentTextInput(int x, int y, int boxLength, int textLength
            , @Nullable TextureRegion btnTex
            , @Nullable TextureRegion btnTexHover
            , @Nullable TextureRegion btnTexDisabled
            , int validColour, int invalidColour
            , Predicate<String> validator, Consumer<String> callback, String initial
    ) {
        this(x, y, boxLength, textLength,
                btnTex, btnTexHover, btnTexDisabled, validColour, invalidColour
                , validator, callback, initial, null);
    }

    public GuiComponentTextInput(int x, int y, int boxLength, int textLength
            , @Nullable TextureRegion btnTex
            , @Nullable TextureRegion btnTexHover
            , @Nullable TextureRegion btnTexDisabled
            , int validColour, int invalidColour
            , Predicate<String> validator, Consumer<String> callback
    ) {
        this(x, y, boxLength, textLength,
                btnTex, btnTexHover, btnTexDisabled, validColour, invalidColour
                , validator, callback, "");
    }

    public void setValue(String value) {
        this.value = value.substring(0, Math.min(value.length(), textLength));
        updateValidity();
    }

    public String getValue() {
        return value;
    }

    private void updateValidity() {
        valid = validator.test(value);
    }

    private void updateValidityAndAccept() {
        updateValidity();
        if (valid) callback.accept(value);
    }

    private boolean isMouseOverButton(int mX, int mY) {
        return GuiUtils.isMouseOver(x + boxLength + 5, y, GuiUtils.getFontHeight() + 4, GuiUtils.getFontHeight() + 4, mX, mY);
    }

    private boolean isMouseOverBox(int mX, int mY) {
        return GuiUtils.isMouseOver(x, y, boxLength + 4, GuiUtils.getFontHeight() + 4, mX, mY);
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public void render(float partialTicks, int mX, int mY, boolean mouseOver) {
        if (btnTex != null)
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
                onTextCharInput(typed);
            } else if (keyCode == Keyboard.KEY_DELETE) {
                value = "";
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
            }
            updateValidityAndAccept();
            return true;
        }
        return false;
    }

    private void onTextCharInput(char typed) {
        switch (fieldType) {
            case DIGITS_ONLY:
                String val = value;
                if (typed >= 48 && typed <= 57) { // digits 0..9
                    if (typed == 48 && val.equals("")) return; // can't add "0" to empty
                    val += typed;
                } else if (typed == 107) { // letter "k", add 000 (multiply by thousand) if can
                    if (val.equals("") || val.equals("0")) {
                        val = "1000";
                    } else {
                        for (int i = 0; i < 3; i++) {
                            if (val.length() >= textLength) break;
                            val += "0";
                        }
                    }
                }

                while (val.length() > 1) {
                    char first = val.charAt(0);
                    if (first < 49 || first > 57) { // not 1..9
                        val = val.substring(1);
                    } else break;
                }
                value = val;
                break;
            case ALL_TEXT:
            default:
                value += typed;
                break;
        }
    }

    public enum FieldType {
        ALL_TEXT, DIGITS_ONLY
    }
}
