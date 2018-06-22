package io.github.phantamanta44.libnine.client;

import io.github.phantamanta44.libnine.block.L9Block;
import io.github.phantamanta44.libnine.block.L9BlockStated;
import io.github.phantamanta44.libnine.block.state.IBlockModelMapper;
import io.github.phantamanta44.libnine.item.L9Item;
import io.github.phantamanta44.libnine.util.tuple.IPair;
import io.github.phantamanta44.libnine.util.tuple.ITriple;
import io.github.phantamanta44.libnine.Registrar;
import io.github.phantamanta44.libnine.Virtue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

public class ClientRegistrar extends Registrar {

    private List<ITriple<Item, Integer, ModelResourceLocation>> rqItemModels = new LinkedList<>();
    private List<IPair<Block, IStateMapper>> rqBlockStateMappers = new LinkedList<>();

    @Override
    public void queueItemModelReg(L9Item item, int meta, String model, String variant) {
        rqItemModels.add(ITriple.of(item, meta, getBound().newModelResourceLocation(model, variant)));
    }

    @Override
    public void queueItemModelReg(L9Item item, int meta, String model) {
        queueItemModelReg(item, meta, model, "inventory");
    }

    @Override
    public void queueItemModelReg(L9Item item, String model, String variant) {
        queueItemModelReg(item, 0, model, variant);
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
        rqItemModels.add(ITriple.of(block.getItemBlock(), meta, getBound().newModelResourceLocation(model)));
    }

    @Override
    public void queueItemBlockModelReg(L9Block block, String model, String variant) {
        queueItemBlockModelReg(block, 0, model, variant);
    }

    @Override
    public void queueItemBlockModelReg(L9Block block, String model) {
        queueItemBlockModelReg(block, 0, model);
    }

    @Override
    public void queueBlockStateMapperReq(L9BlockStated block, IBlockModelMapper mapper) {
        rqBlockStateMappers.add(IPair.of(block, new StateMapperAdapter(mapper, getBound())));
    }

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        rqItemModels.forEach(m -> m.sprexec(ModelLoader::setCustomModelResourceLocation));
        rqItemModels = null;
        rqBlockStateMappers.forEach(m -> m.sprexec(ModelLoader::setCustomStateMapper));
        rqBlockStateMappers = null;
    }

    private static class StateMapperAdapter extends StateMapperBase {

        private final IBlockModelMapper mapper;
        private final Virtue virtue;

        StateMapperAdapter(IBlockModelMapper mapper, Virtue virtue) {
            this.mapper = mapper;
            this.virtue = virtue;
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            String variant = mapper.getVariant(state);
            return variant == null
                    ? virtue.newModelResourceLocation(mapper.getModel(state))
                    : virtue.newModelResourceLocation(mapper.getModel(state), variant);
        }

    }

}
