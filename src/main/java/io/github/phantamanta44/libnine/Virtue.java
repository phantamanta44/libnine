package io.github.phantamanta44.libnine;

import io.github.phantamanta44.libnine.block.L9Block;
import io.github.phantamanta44.libnine.item.L9Item;
import io.github.phantamanta44.libnine.network.PacketServerSyncTileEntity;
import io.github.phantamanta44.libnine.util.LazyConstant;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Virtue {

    private static final Map<String, Virtue> loadedVirtues = new LinkedHashMap<>();

    public static Virtue forMod(String modId) {
        return loadedVirtues.get(modId);
    }

    private final String modId, modPref;
    private final CreativeTabs defaultCreativeTab;
    private final LazyConstant<SimpleNetworkWrapper> networkHandler;
    private boolean usesTileEntities;

    public Virtue(String modId, @Nullable CreativeTabs defaultCreativeTab) {
        this.modId = modId;
        this.modPref = modId + ":";
        this.defaultCreativeTab = defaultCreativeTab;
        this.networkHandler = new LazyConstant<>(() -> NetworkRegistry.INSTANCE.newSimpleChannel(modId));
        this.usesTileEntities = false;
        loadedVirtues.put(modId, this);
    }

    public Virtue(String modId) {
        this(modId, null);
    }

    public String getModId() {
        return modId;
    }

    @Nullable
    public CreativeTabs getDefaultCreativeTab() {
        return defaultCreativeTab;
    }

    public SimpleNetworkWrapper getNetworkHandler() {
        return networkHandler.get();
    }

    public String prefix(String suffix) {
        return modPref + suffix;
    }

    public ResourceLocation newResourceLocation(String resource) {
        return new ResourceLocation(modId, resource);
    }

    public ModelResourceLocation newModelResourceLocation(String model, String variant) {
        return new ModelResourceLocation(prefix(model), variant);
    }

    public ModelResourceLocation newModelResourceLocation(String model) {
        return new ModelResourceLocation(prefix(model));
    }

    public void setCreativeTabFor(L9Item item) {
        if (defaultCreativeTab != null) {
            item.setCreativeTab(defaultCreativeTab);
        }
    }

    public void setCreativeTabFor(L9Block block) {
        if (defaultCreativeTab != null) {
            block.setCreativeTab(defaultCreativeTab);
        }
    }

    void markUsesTileEntities() {
        if (!usesTileEntities) {
            getNetworkHandler().registerMessage(
                    PacketServerSyncTileEntity.Handler.class, PacketServerSyncTileEntity.class, 255, Side.CLIENT);
            usesTileEntities = true;
        }
    }

}
