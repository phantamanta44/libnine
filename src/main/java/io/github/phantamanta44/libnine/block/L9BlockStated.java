package io.github.phantamanta44.libnine.block;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.block.state.VirtualState;
import io.github.phantamanta44.libnine.item.L9ItemBlock;
import io.github.phantamanta44.libnine.item.L9ItemBlockStated;
import io.github.phantamanta44.libnine.util.collection.Accrue;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class L9BlockStated extends L9Block {

    @SuppressWarnings("NullableProblems")
    private List<IProperty<?>> props;
    @SuppressWarnings("NullableProblems")
    private List<VirtualState> states;
    @SuppressWarnings("NullableProblems")
    private TObjectIntMap<IBlockState> statesInv;

    public L9BlockStated(String name, Material material) {
        super(name, material);
        setDefaultState(initDefaultState(getBlockState().getBaseState()));
    }

    /*
     * Initializers
     */

    @SuppressWarnings("WeakerAccess")
    protected void accrueProperties(Accrue<IProperty<?>> props) {
        // NO-OP
    }

    @SuppressWarnings("WeakerAccess")
    protected void accrueVolatileProperties(Accrue<IProperty<?>> props) {
        // NO-OP
    }

    @Override
    protected BlockStateContainer createBlockState() {
        List<IProperty<?>> propList = new ArrayList<>();
        Accrue<IProperty<?>> accum = new Accrue<>(propList);
        accrueProperties(accum);
        states = Collections.unmodifiableList(VirtualState.cartesian(propList));
        accrueVolatileProperties(accum);
        props = Collections.unmodifiableList(propList);
        BlockStateContainer container = new BlockStateContainer(this, props.toArray(new IProperty[0]));
        statesInv = new TObjectIntHashMap<>();
        for (int i = 0; i < states.size(); i++) {
            statesInv.put(states.get(i).synthesize(container), i);
        }
        return container;
    }

    @SuppressWarnings("WeakerAccess")
    protected IBlockState initDefaultState(IBlockState state) {
        return state;
    }

    @Override
    protected L9ItemBlock initItemBlock() {
        return new L9ItemBlockStated(this);
    }

    @Override
    protected void initModel() {
        for (int i = 0; i < states.size(); i++) {
            LibNine.PROXY.getRegistrar()
                    .queueItemBlockModelReg(this, i, getItemBlock().getModelName(states.get(i)));
        }
    }

    /*
     * Properties
     */

    public List<IProperty<?>> getProperties() {
        return props;
    }

    public List<VirtualState> getStates() {
        return states;
    }

    @Override
    public L9ItemBlockStated getItemBlock() {
        return (L9ItemBlockStated)super.getItemBlock();
    }

    /*
     * Behaviour
     */

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < states.size(); i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return statesInv.get(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return (meta >= 0 && meta < states.size()) ? states.get(meta).synthesize(getBlockState()) : getDefaultState();
    }

}
