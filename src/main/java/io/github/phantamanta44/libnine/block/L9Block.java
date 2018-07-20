package io.github.phantamanta44.libnine.block;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.item.L9ItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class L9Block extends Block implements ITileEntityProvider {

    private final String internalName;
    private final L9ItemBlock itemBlock;

    @Nullable
    private BiFunction<World, Integer, ? extends TileEntity> tileFactory;

    public L9Block(String name, Material material) {
        super(material);
        this.internalName = name;
        initName();
        this.itemBlock = initItemBlock();
        initRegistration();
    }

    public void postInit() {
        initModel();
        initCreativeTab();
    }

    /*
     * Initializers
     */

    protected void initName() {
        setUnlocalizedName(LibNine.PROXY.getRegistrar().getBound().prefix(getInternalName()));
    }

    protected L9ItemBlock initItemBlock() {
        return new L9ItemBlock(this);
    }

    protected void initRegistration() {
        ResourceLocation registryName = LibNine.PROXY.getRegistrar().getBound().newResourceLocation(getInternalName());
        setRegistryName(registryName);
        getItemBlock().setRegistryName(registryName);
        LibNine.PROXY.getRegistrar().queueBlockReg(this);
        LibNine.PROXY.getRegistrar().queueItemReg(getItemBlock());
    }

    protected void initModel() {
        LibNine.PROXY.getRegistrar().queueItemBlockModelReg(this, getInternalName());
    }

    protected void initCreativeTab() {
        LibNine.PROXY.getRegistrar().getBound().setCreativeTabFor(this);
    }

    /*
     * Optional initializers
     */

    protected void setTileFactory(BiFunction<World, Integer, ? extends TileEntity> tileFactory) {
        this.tileFactory = tileFactory;
    }

    /*
     * Properties
     */

    public L9ItemBlock getItemBlock() {
        return itemBlock;
    }

    public String getInternalName() {
        return internalName;
    }

    /*
     * Behaviour
     */

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return tileFactory != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, getMetaFromState(state));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return tileFactory != null ? tileFactory.apply(world, meta) : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends TileEntity> T getTileEntity(IBlockAccess world, BlockPos pos) {
        return (T)world.getTileEntity(pos);
    }

}
