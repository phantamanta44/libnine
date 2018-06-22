package io.github.phantamanta44.libnine;

import io.github.phantamanta44.libnine.block.L9Block;
import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.block.state.IBlockModelMapper;
import io.github.phantamanta44.libnine.item.L9Item;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.util.LazyConstant;
import io.github.phantamanta44.libnine.util.helper.FormatUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Registrar {

    private final Map<Class<? extends L9TileEntity>, Virtue> tileVirtueTable;

    public Registrar() {
        tileVirtueTable = new HashMap<>();
    }

    public Virtue lookUpTileVirtue(Class<? extends L9TileEntity> clazz) {
        return tileVirtueTable.get(clazz);
    }

    private Virtue bound;
    private List<L9Block> virtueBlocks = new LinkedList<>();
    private List<L9Item> virtueItems = new LinkedList<>();
    
    public void begin(Virtue virtue) {
        if (getBound() != null) {
            throw new IllegalStateException(String.format("Could not bind virtue %s because %s was already bound!",
                            virtue.getModId(), getBound().getModId()));
        }
        this.bound = virtue;
    }

    public void end() {
        if (getBound() == null) {
            throw new IllegalStateException("Nothing is bound!");
        }
        virtueItems.forEach(L9Item::postInit);
        virtueItems.clear();
        virtueBlocks.forEach(L9Block::postInit);
        virtueBlocks.clear();
        this.bound = null;
    }

    public Virtue getBound() {
        return bound;
    }

    private List<Item> rqItems = new LinkedList<>();

    public void queueItemReg(Item item) {
        rqItems.add(item);
        if (item instanceof L9Item) virtueItems.add((L9Item)item);
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        rqItems.forEach(event.getRegistry()::register);
        rqItems = null;
    }

    private List<L9Block> rqBlocks = new LinkedList<>();
    private List<TileRegistration> rqTileEntities = new LinkedList<>();

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
//        rqBlocks.forEach(event.getRegistry()::register);
        for (L9Block b : rqBlocks) {
            System.out.println("Registering: " + b.getClass().getName());
            event.getRegistry().register(b);
        }
        rqBlocks = null;
        rqTileEntities.forEach(t -> {
            t.register();
            tileVirtueTable.put(t.clazz.get(), t.virtue.get());
        });
        rqTileEntities = null;
    }
    
    public void queueItemModelReg(L9Item item, int meta, String model, String variant) {
        // NO-OP
    }
    
    public void queueItemModelReg(L9Item item, int meta, String model) {
        // NO-OP
    }

    public void queueItemModelReg(L9Item item, String model, String variant) {
        // NO-OP
    }

    public void queueItemModelReg(L9Item item, String model) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, int meta, String model, String variant) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, int meta, String model) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, String model, String variant) {
        // NO-OP
    }

    public void queueItemBlockModelReg(L9Block block, String model) {
        // NO-OP
    }
    
    public void queueBlockStateMapperReq(L9BlockStated block, IBlockModelMapper mapper) {
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
