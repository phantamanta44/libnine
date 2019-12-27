package io.github.phantamanta44.libnine.event;

import io.github.phantamanta44.libnine.tile.L9TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class TileEntityDispatchHandler {

    private final Set<L9TileEntity> toUpdate = new HashSet<>();

    public void queueTileUpdate(L9TileEntity tile) {
        toUpdate.add(tile);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!toUpdate.isEmpty()) {
            for (L9TileEntity tile : toUpdate) {
                World world = tile.getWorld();
                //noinspection ConstantConditions
                if (world != null && world.getTileEntity(tile.getPos()) == tile) {
                    tile.dispatchTileUpdate();
                }
            }
            toUpdate.clear();
        }
    }

}
