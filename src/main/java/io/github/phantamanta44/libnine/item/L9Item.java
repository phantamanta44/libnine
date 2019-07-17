package io.github.phantamanta44.libnine.item;

import io.github.phantamanta44.libnine.LibNine;
import net.minecraft.item.Item;

public class L9Item extends Item {

    private final String internalName;

    public L9Item(String name) {
        this.internalName = name;
        initName();
        initRegistration();
    }

    public void postInit() {
        initModel();
        initCreativeTab();
    }

    /*
     * Initializers
     */

    protected void initName() {
        setTranslationKey(LibNine.PROXY.getRegistrar().getBound().prefix(getInternalName()));
    }

    protected void initRegistration() {
        setRegistryName(LibNine.PROXY.getRegistrar().getBound().newResourceLocation(getInternalName()));
        LibNine.PROXY.getRegistrar().queueItemReg(this);
    }

    protected void initModel() {
        LibNine.PROXY.getRegistrar().queueItemModelReg(this, getInternalName());
    }

    protected void initCreativeTab() {
        LibNine.PROXY.getRegistrar().getBound().setCreativeTabFor(this);
    }

    /*
     * Properties
     */

    public String getInternalName() {
        return internalName;
    }

}
