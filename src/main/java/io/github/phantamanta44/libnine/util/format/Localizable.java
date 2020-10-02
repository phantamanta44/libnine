package io.github.phantamanta44.libnine.util.format;

import net.minecraft.util.text.translation.I18n;

public interface Localizable {

    String getTranslationKey();

    @SuppressWarnings("deprecation")
    default String getLocalizedName() {
        return I18n.translateToLocal(getTranslationKey());
    }

}
