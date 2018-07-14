package io.github.phantamanta44.libnine.util.world;

import io.github.phantamanta44.libnine.util.ImpossibilityRealizedException;
import net.minecraft.util.EnumFacing;

import java.util.function.UnaryOperator;

public enum BlockSide {

    FRONT(f -> f),
    BACK(EnumFacing::getOpposite),
    UP(f -> EnumFacing.UP),
    LEFT(EnumFacing::rotateY),
    DOWN(f -> EnumFacing.DOWN),
    RIGHT(EnumFacing::rotateYCCW);

    private final UnaryOperator<EnumFacing> transformer;

    BlockSide(UnaryOperator<EnumFacing> transformer) {
        this.transformer = transformer;
    }

    public EnumFacing getDirection(EnumFacing front) {
        return transformer.apply(front);
    }

    public static BlockSide fromDirection(EnumFacing front, EnumFacing face) {
        for (BlockSide side : values()) {
            if (side.getDirection(front) == face) return side;
        }
        throw new ImpossibilityRealizedException();
    }

}
