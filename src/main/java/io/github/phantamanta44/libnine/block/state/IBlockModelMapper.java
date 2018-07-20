package io.github.phantamanta44.libnine.block.state;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface IBlockModelMapper {

    static IBlockModelMapper toModel(Function<IBlockState, String> mapper) {
        return new ToModel(mapper);
    }

    static IBlockModelMapper toVariant(String model, Function<IBlockState, String> mapper) {
        return new ToVariant(model, mapper);
    }

    String getModel(IBlockState state);

    @Nullable
    String getVariant(IBlockState state);

    class ToModel implements IBlockModelMapper {

        private final Function<IBlockState, String> mapper;

        ToModel(Function<IBlockState, String> mapper) {
            this.mapper = mapper;
        }

        @Override
        public String getModel(IBlockState state) {
            return mapper.apply(state);
        }

        @Nullable
        @Override
        public String getVariant(IBlockState state) {
            return "normal";
        }

    }

    class ToVariant implements IBlockModelMapper {

        private final String model;
        private final Function<IBlockState, String> mapper;

        ToVariant(String model, Function<IBlockState, String> mapper) {
            this.model = model;
            this.mapper = mapper;
        }

        @Override
        public String getModel(IBlockState state) {
            return model;
        }

        @Nullable
        @Override
        public String getVariant(IBlockState state) {
            return mapper.apply(state);
        }

    }

}
