package io.github.phantamanta44.libnine.client;

import io.github.phantamanta44.libnine.Registrar;
import io.github.phantamanta44.libnine.Virtue;
import io.github.phantamanta44.libnine.block.L9Block;
import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.block.state.IBlockModelMapper;
import io.github.phantamanta44.libnine.gui.GuiIdentity;
import io.github.phantamanta44.libnine.gui.L9GuiHandler;
import io.github.phantamanta44.libnine.item.L9Item;
import io.github.phantamanta44.libnine.util.tuple.IPair;
import io.github.phantamanta44.libnine.util.tuple.ITriple;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClientRegistrar extends Registrar {

    private final List<ITriple<Item, Integer, ModelResourceLocation>> rqItemModels = new LinkedList<>();
    private final List<IPair<Block, IStateMapper>> rqBlockStateMappers = new LinkedList<>();

    @Override
    public void queueItemModelReg(L9Item item, int meta, String model, String variant) {
        rqItemModels.add(ITriple.of(item, meta, getBound().newModelResourceLocation(model, variant)));
    }

    @Override
    public void queueItemModelReg(L9Item item, int meta, String model) {
        queueItemModelReg(item, meta, model, "inventory");
    }

    @Override
    public void queueItemModelReg(L9Item item, String model) {
        queueItemModelReg(item, 0, model);
    }

    @Override
    public void queueItemBlockModelReg(L9Block block, int meta, String model, String variant) {
        rqItemModels.add(ITriple.of(block.getItemBlock(), meta, getBound().newModelResourceLocation(model, variant)));
    }

    @Override
    public void queueItemBlockModelReg(L9Block block, int meta, String model) {
        queueItemBlockModelReg(block, meta, model, "inventory");
    }

    @Override
    public void queueItemBlockModelReg(L9Block block, String model) {
        queueItemBlockModelReg(block, 0, model);
    }

    @Override
    public void queueBlockStateMapperReg(L9BlockStated block, IBlockModelMapper mapper) {
        rqBlockStateMappers.add(IPair.of(block, new StateMapperAdapter(mapper, getBound())));
    }

    private final List<IPair<IItemColor, Item[]>> rqItemColourHandlers = new LinkedList<>();
    private final List<IPair<IBlockColor, L9Block[]>> rqBlockColourHandlers = new LinkedList<>();

    @Override
    public void queueItemColourHandlerReg(IItemColor handler, Item... items) {
        rqItemColourHandlers.add(IPair.of(handler, items));
    }

    @Override
    public void queueBlockColourHandlerReg(IBlockColor handler, L9Block... blocks) {
        rqBlockColourHandlers.add(IPair.of(handler, blocks));
    }

    @Override
    public void onRegisterColourHandlers() {
        rqItemColourHandlers.forEach(h -> h.sprexec(
                Minecraft.getMinecraft().getItemColors()::registerItemColorHandler));
        rqBlockColourHandlers.forEach(h -> h.sprexec(
                Minecraft.getMinecraft().getBlockColors()::registerBlockColorHandler));
    }

    @Override
    public <T extends TileEntity> void queueTESRReg(Class<T> clazz, TileEntitySpecialRenderer<T> renderer) {
        ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
    }

    @Override
    public <S extends Container, C> void queueGuiClientReg(GuiIdentity<S, C> identity, L9GuiHandler.IGuiFactory<S, C> factory) {
        getBound().getGuiHandler().registerClientGui(identity, factory);
    }

    @Override
    protected void hookEvents() {
        super.hookEvents();
        MinecraftForge.EVENT_BUS.register(new ClientRegistrarHook());
    }

    private class ClientRegistrarHook {

        @SubscribeEvent
        public void onRegisterModels(ModelRegistryEvent event) {
            rqItemModels.forEach(m -> m.sprexec(ModelLoader::setCustomModelResourceLocation));
            rqBlockStateMappers.forEach(m -> m.sprexec(ModelLoader::setCustomStateMapper));
        }

    }

    private static class StateMapperAdapter extends StateMapperBase {

        private final IBlockModelMapper mapper;
        private final Virtue virtue;

        StateMapperAdapter(IBlockModelMapper mapper, Virtue virtue) {
            this.mapper = mapper;
            this.virtue = virtue;
        }

        @Override
        public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
            for (IBlockState state : block.getBlockState().getValidStates()) {
                mapStateModelLocations.put(state, getModelResourceLocation(state));
            }
            return mapStateModelLocations;
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            String model = mapper.getModel(state);
            String variant = mapper.getVariant(state);
            return variant == null
                    ? virtue.newModelResourceLocation(model)
                    : virtue.newModelResourceLocation(model, variant);
        }

    }

}
