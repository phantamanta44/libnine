package io.github.phantamanta44.libnine.block;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.block.state.VirtualState;
import io.github.phantamanta44.libnine.item.L9ItemBlock;
import io.github.phantamanta44.libnine.item.L9ItemBlockStated;
import io.github.phantamanta44.libnine.util.Accrue;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class L9BlockStated extends L9Block {

    private List<IProperty<?>> props;
    private List<VirtualState> states;

    public L9BlockStated(String name, Material material) {
        super(name, material);
    }

    /*
     * Initializers
     */

    protected void accrueProperties(Accrue<IProperty<?>> props) {
        // NO-OP
    }

    protected void accrueVolatileProperties(Accrue<IProperty<?>> props) {
        // NO-OP
    }

    @Override
    protected BlockStateContainer createBlockState() {
        List<IProperty<?>> propList = new LinkedList<>();
        Accrue<IProperty<?>> accum = new Accrue<>(propList);
        accrueProperties(accum);
        states = Collections.unmodifiableList(VirtualState.cartesian(propList));
        accrueVolatileProperties(accum);
        props = Collections.unmodifiableList(propList);
        return new BlockStateContainer(this, props.toArray(new IProperty[0]));
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
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).matches(state)) return i;
        }
        throw new IllegalArgumentException("Invalid block state!");
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return states.get(meta).synthesize(getBlockState());
    }

}
