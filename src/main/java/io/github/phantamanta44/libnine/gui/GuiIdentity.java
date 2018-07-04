package io.github.phantamanta44.libnine.gui;

import net.minecraft.inventory.Container;

import javax.annotation.Nullable;

public class GuiIdentity<S extends Container, C> {

    private final String name;
    private final Class<? extends L9Container> containerClass;

    public GuiIdentity(String name, @Nullable Class<? extends L9Container> containerClass) {
        this.name = name;
        this.containerClass = containerClass;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Class<? extends L9Container> getContainerClass() {
        return containerClass;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GuiIdentity && ((GuiIdentity)o).name == name;
    }

}
