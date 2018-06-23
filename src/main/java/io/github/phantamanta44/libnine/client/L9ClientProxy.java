package io.github.phantamanta44.libnine.client;

import io.github.phantamanta44.libnine.L9CommonProxy;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.Registrar;

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

}
