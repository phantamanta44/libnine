package io.github.phantamanta44.libnine.component.multiblock;

import io.github.phantamanta44.libnine.util.data.ByteUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.*;

public class MultiBlockCore<T extends IMultiBlockUnit<T>> extends MultiBlockConnectable<T> {

    private boolean formed = false;
    private final List<Runnable> formationStatusCallbacks = new ArrayList<>();

    public MultiBlockCore(T component, MultiBlockType<T> type) {
        super(component, type);
    }

    @Nullable
    @Override
    public MultiBlockCore<T> getCore() {
        return this;
    }

    @Override
    public void setCore(@Nullable MultiBlockCore<T> core) {
        throw new UnsupportedOperationException();
    }

    public boolean isFormed() {
        return formed;
    }

    public void onFormationStatusChange(Runnable callback) {
        formationStatusCallbacks.add(callback);
    }

    private void postFormationStatusChange() {
        formationStatusCallbacks.forEach(Runnable::run);
    }

    public boolean tryForm() {
        if (!formed) {
            Deque<SearchNode<T>> searchQueue = new ArrayDeque<>();
            searchQueue.offer(new SearchNode<>(this, null, 0));
            while (!searchQueue.isEmpty()) {
                SearchNode<T> searchNode = searchQueue.pop();
                for (EnumFacing dir : EnumFacing.VALUES) {
                    if (searchNode.canTraverse(dir)) {
                        switch (searchNode.connectable.tryEmit(dir)) {
                            case SUCCESS:
                                if (searchNode.distance < getType().getMaxSearchDist()) {
                                    searchQueue.offer(new SearchNode<>(
                                            Objects.requireNonNull(searchNode.connectable.getAdjacent(dir)),
                                            dir.getOpposite(),
                                            searchNode.distance + 1));
                                }
                                break;
                            case CONFLICT:
                                destroyTree();
                                return false;
                        }
                    }
                }
            }
            if (!getType().checkStructure(this)) {
                destroyTree();
                return false;
            }
            formed = true;
            postFormationStatusChange();
        }
        return true;
    }

    private void destroyTree() {
        Deque<MultiBlockConnectable<T>> searchQueue = new ArrayDeque<>();
        searchQueue.offer(this);
        while (!searchQueue.isEmpty()) {
            MultiBlockConnectable<T> connectable = searchQueue.pop();
            for (EnumFacing dir : connectable.getEmittingDirs()) {
                MultiBlockConnectable<T> adjacent = connectable.getAdjacent(dir);
                if (adjacent != null) {
                    searchQueue.offer(adjacent);
                }
            }
            connectable.clearEmission();
        }
    }

    @Override
    public void disconnect() {
        if (formed) {
            destroyTree();
            formed = false;
            postFormationStatusChange();
        }
    }

    @Override
    public void serBytes(ByteUtils.Writer data) {
        super.serBytes(data);
        data.writeBool(formed);
    }

    @Override
    public void deserBytes(ByteUtils.Reader data) {
        super.deserBytes(data);
        formed = data.readBool();
    }

    @Override
    public void serNBT(NBTTagCompound tag) {
        super.serNBT(tag);
        tag.setBoolean("Formed", formed);
    }

    @Override
    public void deserNBT(NBTTagCompound tag) {
        super.deserNBT(tag);
        formed = tag.getBoolean("Formed");
    }

    private static class SearchNode<T extends IMultiBlockUnit<T>> {

        final MultiBlockConnectable<T> connectable;
        @Nullable
        final EnumFacing fromDir;
        final int distance;

        SearchNode(MultiBlockConnectable<T> connectable, @Nullable EnumFacing fromDir, int distance) {
            this.connectable = connectable;
            this.fromDir = fromDir;
            this.distance = distance;
        }

        boolean canTraverse(EnumFacing dir) {
            return dir != fromDir;
        }

    }

}
