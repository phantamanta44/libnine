package io.github.phantamanta44.libnine.util.math;

import io.github.phantamanta44.libnine.util.tuple.IPair;
import net.minecraft.util.math.Vec3i;

public class MathUtils {

    public static final double R2D_D = 180D / Math.PI;
    public static final double D2R_D = Math.PI / 180D;
    public static final float PI_F = 3.14159265358979F;
    public static final float R2D_F = 180F / PI_F;
    public static final float D2R_F = PI_F / 180F;
    public static final float EPS_F = 1e-15F;
    public static final double EPS_D = 1e-24D;
    
    public static boolean fpEquals(float a, float b) {
        return Math.abs(a - b) <= EPS_F;
    }

    public static boolean fpEquals(double a, double b) {
        return Math.abs(a - b) <= EPS_D;
    }

    public static int clamp(int n, int lower, int upper) {
        return Math.max(Math.min(n, upper), lower);
    }

    public static float clamp(float n, float lower, float upper) {
        return Math.max(Math.min(n, upper), lower);
    }

    public static double clamp(double n, double lower, double upper) {
        return Math.max(Math.min(n, upper), lower);
    }

    public static long clamp(long n, long lower, long upper) {
        return Math.max(Math.min(n, upper), lower);
    }

    public static IPair<Vec3i, Vec3i> computeCuboid(Iterable<Vec3i> points) {
        int minX = 0, minY = 0, minZ = 0;
        int maxX = 0, maxY = 0, maxZ = 0;
        for (Vec3i point : points) {
            int x = point.getX(), y = point.getY(), z = point.getZ();
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            } else if (z > maxZ) {
                maxZ = z;
            }
        }
        return IPair.of(new Vec3i(minX, minY, minZ), new Vec3i(maxX, maxY, maxZ));
    }

    public static Vec3i add(Vec3i a, int x, int y, int z) {
        return new Vec3i(a.getX() + x, a.getY() + y, a.getZ() + z);
    }

    public static Vec3i add(Vec3i a, Vec3i b) {
        return add(a, b.getX(), b.getY(), b.getZ());
    }

    public static Vec3i subtract(Vec3i a, int x, int y, int z) {
        return new Vec3i(a.getX() - x, a.getY() - y, a.getZ() - z);
    }

    public static Vec3i subtract(Vec3i a, Vec3i b) {
        return subtract(a, b.getX(), b.getY(), b.getZ());
    }

}
