package io.github.phantamanta44.libnine;

import io.github.phantamanta44.libnine.network.PacketServerSyncTileEntity;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.tile.RegisterTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class L9CommonProxy {

    private final Registrar registrar;

    public L9CommonProxy() {
        registrar = initRegistrar();
    }

    /*
     * Internal
     */

    protected Registrar initRegistrar() {
        return new Registrar();
    }

    /*
     * API
     */

    public Registrar getRegistrar() {
        return registrar;
    }

    public void dispatchTileUpdate(L9TileEntity tile) {
        BlockPos pos = tile.getPos();
        getRegistrar().lookUpTileVirtue(tile.getClass()).getNetworkHandler().sendToAllAround(
                new PacketServerSyncTileEntity(tile),
                new NetworkRegistry.TargetPoint(
                        tile.getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64D));
    }

    public World getAnySidedWorld() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
    }

    /*
     * Callbacks
     */

    protected void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(registrar);
        for (ASMDataTable.ASMData target : event.getAsmData().getAll(RegisterTile.class.getName())) {
            getRegistrar().queueTileEntityReg((String)target.getAnnotationInfo().get("value"), target.getClassName());
        }
    }

    protected void onInit(FMLInitializationEvent event) {
        // NO-OP
    }

    protected void onPostInit(FMLPostInitializationEvent event) {
        // NO-OP
    }

}
