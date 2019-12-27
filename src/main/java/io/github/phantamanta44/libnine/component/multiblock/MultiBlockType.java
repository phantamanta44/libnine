package io.github.phantamanta44.libnine.component.multiblock;

import io.github.phantamanta44.libnine.util.world.structmatcher.IStructureMatcher;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MultiBlockType<T extends IMultiBlockUnit<T>> {

    private final ResourceLocation id;
    private final int maxSearchDist;
    private final Class<T> componentType;

    @Nullable
    private IStructureMatcher structureMatcher;

    public MultiBlockType(ResourceLocation id, int maxSearchDist, Class<T> componentType) {
        this.id = id;
        this.maxSearchDist = maxSearchDist;
        this.componentType = componentType;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public T checkComponent(@Nullable Object obj) {
        return componentType.isInstance(obj) ? (T)obj : null;
    }

    public int getMaxSearchDist() {
        return maxSearchDist;
    }

    public void setStructureMatcher(IStructureMatcher structureMatcher) {
        this.structureMatcher = structureMatcher;
    }

    public boolean checkStructure(MultiBlockCore<T> core) {
        if (structureMatcher == null) {
            return true;
        }
        WorldBlockPos basePos = core.getUnit().getWorldPos();
        return structureMatcher.testStructure(basePos, StreamSupport.stream(core.spliterator(), false)
                .map(c -> c.getUnit().getWorldPos().getPos().subtract(basePos.getPos()))
                .collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MultiBlockType && id.equals(((MultiBlockType)obj).id);
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
