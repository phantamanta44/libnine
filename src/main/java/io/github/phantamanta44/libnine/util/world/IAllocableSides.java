package io.github.phantamanta44.libnine.util.world;

public interface IAllocableSides<E extends Enum<E>> {

    void setFace(BlockSide face, E state);

    E getFace(BlockSide face);

}
