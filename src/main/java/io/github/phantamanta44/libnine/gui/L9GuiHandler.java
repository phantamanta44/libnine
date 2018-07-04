package io.github.phantamanta44.libnine.gui;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.github.phantamanta44.libnine.Virtue;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class L9GuiHandler implements IGuiHandler {

    private final Virtue virtue;
    private final TObjectIntMap<GuiIdentity> guiIdentityMappings;
    private final TIntObjectMap<ContainerFactory> serverGuiMappings;
    private final TIntObjectMap<GuiFactory> clientGuiMappings;
    private int currentIndex;

    public L9GuiHandler(Virtue virtue) {
        this.virtue = virtue;
        this.guiIdentityMappings = new TObjectIntHashMap<>();
        this.serverGuiMappings = new TIntObjectHashMap<>();
        this.clientGuiMappings = new TIntObjectHashMap<>();
        this.currentIndex = -1;
    }

    private int indexFor(GuiIdentity identity) {
        if (!guiIdentityMappings.containsKey(identity)) {
            guiIdentityMappings.put(identity, ++currentIndex);
            return currentIndex;
        }
        return guiIdentityMappings.get(identity);
    }

    public <T extends Container> void registerServerGui(GuiIdentity<T, ?> identity, ContainerFactory<T> factory) {
        serverGuiMappings.put(indexFor(identity), factory);
    }

    public <S extends Container, C> void registerClientGui(GuiIdentity<S, C> identity, GuiFactory<S, C> factory) {
        clientGuiMappings.put(indexFor(identity), factory);
    }
    
    public int getGuiId(GuiIdentity<?, ?> identity) {
        return guiIdentityMappings.get(identity);
    }

    public void openGui(EntityPlayer player, GuiIdentity<?, ?> identity, WorldBlockPos pos) {
        player.openGui(virtue, getGuiId(identity), pos.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    @Override
    public Container getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return serverGuiMappings.get(id).create(player, world, x, y, z);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return clientGuiMappings.get(id).create(
                serverGuiMappings.get(id).create(player, world, x, y, z),
                player, world, x, y, z);
    }

    @FunctionalInterface
    public interface ContainerFactory<T extends Container> {

        T create(EntityPlayer player, World world, int x, int y, int z);

    }

    @FunctionalInterface
    public interface GuiFactory<S extends Container, C> {

        C create(S container, EntityPlayer player, World world, int x, int y, int z);

    }

}
