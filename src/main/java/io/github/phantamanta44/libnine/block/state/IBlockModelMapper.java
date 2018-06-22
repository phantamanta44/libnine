package io.github.phantamanta44.libnine.block.state;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;

public interface IBlockModelMapper {

    String getModel(IBlockState state);

    @Nullable
    String getVariant(IBlockState state);

}
