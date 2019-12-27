package io.github.phantamanta44.libnine.util.world.structmatcher;

import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public interface IStructureMatcher {

    boolean testStructure(WorldBlockPos basePos, List<Vec3i> components);

}
