package io.github.phantamanta44.libnine;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = L9Const.MOD_ID, version = L9Const.VERSION, useMetadata = true)
public class LibNine {

    @Mod.Instance(L9Const.MOD_ID)
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static LibNine INSTANCE;

    @SidedProxy(
            serverSide = "io.github.phantamanta44.libnine.L9CommonProxy",
            clientSide = "io.github.phantamanta44.libnine.client.L9ClientProxy"
    )
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static L9CommonProxy PROXY;

    @SuppressWarnings("NotNullFieldNotInitialized")
    public static Logger LOGGER;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        PROXY.onPreInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        PROXY.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        PROXY.onLoadComplete(event);
    }

}
