package io.github.phantamanta44.libnine.util.math;

import net.minecraft.util.math.Vec3d;

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

}
