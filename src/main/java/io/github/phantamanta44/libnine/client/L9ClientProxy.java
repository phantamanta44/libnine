package io.github.phantamanta44.libnine.client;

import io.github.phantamanta44.libnine.L9CommonProxy;
import io.github.phantamanta44.libnine.Registrar;
import io.github.phantamanta44.libnine.client.event.ClientTickHandler;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class L9ClientProxy extends L9CommonProxy {

    /*
     * Internal
     */

    @Override
    protected Registrar initRegistrar() {
        return new ClientRegistrar();
    }

    /*
     * API
     */

    @Override
    public void dispatchTileUpdate(L9TileEntity tile) {
        if (!tile.getWorld().isRemote) super.dispatchTileUpdate(tile);
    }

    /*
     * Callbacks
     */

    @Override
    protected void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
    }

    @Override
    protected void onInit(FMLInitializationEvent event) {
        super.onInit(event);
        getRegistrar().onRegisterColourHandlers();
    }

}
