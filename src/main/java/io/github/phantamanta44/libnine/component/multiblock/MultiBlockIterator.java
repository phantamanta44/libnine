package io.github.phantamanta44.libnine.component.multiblock;

import net.minecraft.util.EnumFacing;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class MultiBlockIterator<T extends IMultiBlockUnit<T>> implements Iterator<MultiBlockConnectable<T>> {

    private final Deque<MultiBlockConnectable<T>> visitQueue = new ArrayDeque<>();

    public MultiBlockIterator(MultiBlockConnectable<T> initial) {
        visitQueue.offer(initial);
    }

    @Override
    public boolean hasNext() {
        return !visitQueue.isEmpty();
    }

    @Override
    public MultiBlockConnectable<T> next() {
        MultiBlockConnectable<T> connectable = visitQueue.pop();
        for (EnumFacing dir : connectable.getEmittingDirs()) {
            MultiBlockConnectable<T> adjacent = connectable.getAdjacent(dir);
            if (adjacent != null) {
                visitQueue.offer(adjacent);
            }
        }
        return connectable;
    }

}
