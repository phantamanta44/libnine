package io.github.phantamanta44.libnine.util.math;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Collectors;

public class LinAlUtils {

    public static final Vec3d X_POS = new Vec3d(1, 0, 0);
    public static final Vec3d X_NEG = new Vec3d(-1, 0, 0);
    public static final Vec3d Y_POS = new Vec3d(0, 1, 0);
    public static final Vec3d Y_NEG = new Vec3d(0, -1, 0);
    public static final Vec3d Z_POS = new Vec3d(0, 0, 1);
    public static final Vec3d Z_NEG = new Vec3d(0, 0, -1);

    public static Vec3d project(Vec3d from, Vec3d onto) {
        Vec3d normBasis = onto.normalize();
        return normBasis.scale(from.dotProduct(normBasis));
    }

    public static Vec3d reflect2D(Vec3d target, Vec3d symmetry) {
        return project(target, symmetry).scale(2D).subtract(target);
    }

    @Nullable
    public static Vec3d castOntoPlane(Vec3d origin, Vec3d dir, Vec3d planarPoint, Vec3d planeNormal) {
        double a = planeNormal.dotProduct(dir);
        if (a == 0) return null;
        double scale = planeNormal.dotProduct(planarPoint.subtract(origin)) / a;
        return scale > 0 ? origin.add(dir.scale(scale)) : null;
    }

    private static final EnumMap<EnumFacing, Vec3d> FACE_VEC_MAPPING = Arrays.stream(EnumFacing.VALUES)
            .collect(Collectors.toMap(d -> d, d -> new Vec3d(d.getDirectionVec()),
                    (a, b) -> b, () -> new EnumMap<>(EnumFacing.class)));

    public static Vec3d getDir(EnumFacing dir) {
        return FACE_VEC_MAPPING.get(dir);
    }

}
