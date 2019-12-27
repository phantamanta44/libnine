package io.github.phantamanta44.libnine.component.multiblock;

import io.github.phantamanta44.libnine.util.world.WorldBlockPos;

public interface IMultiBlockUnit<T extends IMultiBlockUnit<T>> {

    MultiBlockType<T> getMultiBlockType();

    MultiBlockConnectable<T> getMultiBlockConnection();

    WorldBlockPos getWorldPos();

}
