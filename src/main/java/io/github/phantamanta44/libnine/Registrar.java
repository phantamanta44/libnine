package io.github.phantamanta44.libnine;

import io.github.phantamanta44.libnine.block.L9Block;
import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.block.state.IBlockModelMapper;
import io.github.phantamanta44.libnine.gui.GuiIdentity;
import io.github.phantamanta44.libnine.gui.L9Container;
import io.github.phantamanta44.libnine.gui.L9GuiHandler;
import io.github.phantamanta44.libnine.item.L9Item;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.util.LazyConstant;
import io.github.phantamanta44.libnine.util.helper.FormatUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Registrar {

    private final Map<Class<? extends L9TileEntity>, Virtue> tileVirtueTable;
    private final Map<Class<? extends L9Container>, Virtue> containerVirtueTable;

    public Registrar() {
        this.tileVirtueTable = new HashMap<>();
        this.containerVirtueTable = new HashMap<>();
    }

    public Virtue lookUpTileVirtue(Class<? extends L9TileEntity> clazz) {
        return tileVirtueTable.get(clazz);
    }

    public Virtue lookUpContainerVirtue(Class<? extends L9Container> clazz) {
        return containerVirtueTable.get(clazz);
    }

    @Nullable
    private Virtue bound;
    private final List<L9Block> virtueBlocks = new LinkedList<>();
    private final List<L9Item> virtueItems = new LinkedList<>();
    
    public void begin(Virtue virtue) {
        if (bound != null) {
            throw new IllegalStateException(String.format("Could not bind virtue %s because %s was already bound!",
                            virtue.getModId(), getBound().getModId()));
        }
        this.bound = virtue;
    }

    public void end() {
        if (bound == null) throw new IllegalStateException("No virtue is bound!");
        virtueItems.forEach(L9Item::postInit);
        virtueItems.clear();
        virtueBlocks.forEach(L9Block::postInit);
        virtueBlocks.clear();
        this.bound = null;
    }

    public Virtue getBound() {
        if (bound == null) throw new IllegalStateException("No virtue is bound!");
        return bound;
    }

    private final List<Item> rqItems = new LinkedList<>();

    public void queueItemReg(Item item) {
        rqItems.add(item);
        if (item instanceof L9Item) virtueItems.add((L9Item)item);
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        rqItems.forEach(event.getRegistry()::register);
    }

    private final List<L9Block> rqBlocks = new LinkedList<>();
    private final List<TileRegistration> rqTileEntities = new LinkedList<>();

    public void queueBlockReg(L9Block block) {
        rqBlocks.add(block);
        virtueBlocks.add(block);
    }

    void queueTileEntityReg(String modId, String className) {
        rqTileEntities.add(new TileRegistration(modId, className));
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        rqBlocks.forEach(event.getRegistry()::register);
        rqTileEntities.forEach(t -> {
            t.register();
            tileVirtueTable.put(t.clazz.get(), t.virtue.get());
        });
    }
    
    public void queueItemModelReg(L9Item item, int meta, String model) {
        // NO-OP
    }

    public void queueItemModelReg(L9Item item, String model) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, int meta, String model) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, String model) {
        // NO-OP
    }
    
    public void queueBlockStateMapperReg(L9BlockStated block, IBlockModelMapper mapper) {
        // NO-OP
    }

    public void queueItemColourHandlerReg(IItemColor handler, Item... items) {
        // NO-OP
    }

    public void queueBlockColourHandlerReg(IBlockColor handler, L9Block... blocks) {
        // NO-OP
    }

    public void onRegisterColourHandlers() {
        // NO-OP
    }

    public <T extends TileEntity> void queueTESRReg(Class<T> clazz, TileEntitySpecialRenderer<T> renderer) {
        // NO-OP
    }

    public <T extends Container> void queueGuiServerReg(GuiIdentity<T, ?> identity, L9GuiHandler.ContainerFactory<T> factory) {
        getBound().markUsesContainers();
        getBound().getGuiHandler().registerServerGui(identity, factory);
        containerVirtueTable.put(identity.getContainerClass(), getBound());
    }

    public <S extends Container, C> void queueGuiClientReg(GuiIdentity<S, C> identity, L9GuiHandler.GuiFactory<S, C> factory) {
        // NO-OP
    }

    private static class TileRegistration {

        private final LazyConstant<Virtue> virtue;
        private final LazyConstant<Class> clazz;

        TileRegistration(String modId, String className) {
            this.virtue = new LazyConstant<>(() -> Virtue.forMod(modId));
            this.clazz = new LazyConstant<>(() -> {
                try {
                    return Class.forName(className);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid tile entity class: " + className, e);
                }
            });
        }

        @SuppressWarnings("unchecked")
        void register() {
            GameRegistry.registerTileEntity((Class<? extends TileEntity>)clazz.get(),
                    virtue.get().newResourceLocation(FormatUtils.formatClassName(clazz.get())));
            virtue.get().markUsesTileEntities();
        }

    }

}
