package io.github.phantamanta44.libnine.util.math;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
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

    public static Vec3d findOrthogonal(Vec3d vec) {
        if (vec.x == 0D) {
            return LinAlUtils.X_POS;
        } else if (vec.y == 0D) {
            return LinAlUtils.Y_POS;
        } else if (vec.z == 0D) {
            return LinAlUtils.Z_POS;
        } else {
            return new Vec3d(vec.x, -vec.y, (vec.y * vec.y - vec.x * vec.x) / vec.z);
        }
    }

    public static Vec3d rotate(Vec3d vec, Vec3d axis, float angle) {
        // https://en.wikipedia.org/wiki/Rodrigues%27_rotation_formula
        double cos = MathHelper.cos(angle), uncos = 1F - cos, sin = MathHelper.sin(angle);
        double dot = vec.dotProduct(axis);
        Vec3d cross = vec.crossProduct(axis);
        return new Vec3d(
                cos * vec.x + sin * cross.x + uncos * dot * axis.x,
                cos * vec.y + sin * cross.y + uncos * dot * axis.y,
                cos * vec.z + sin * cross.z + uncos * dot * axis.z);
    }

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

    public static boolean intersectsLine(AxisAlignedBB prism, Vec3d lineMin, Vec3d lineMax) {
        double x1 = lineMin.x, x2 = lineMax.x, dx = x2 - x1;
        double y1 = lineMin.y, y2 = lineMax.y, dy = y2 - y1;
        double z1 = lineMin.z, z2 = lineMax.z, dz = z2 - z1;
        double dydx = dy / dx, dzdx = dz / dx, dzdy = dz / dy;
        double inter1, inter2;
        // project to xy plane
        inter1 = (prism.minX - x1) * dydx + y1;
        inter2 = (prism.maxX - x1) * dydx + y1;
        if (Math.min(inter1, inter2) > prism.maxY || Math.max(inter1, inter2) < prism.minY) return false;
        // project to xz plane
        inter1 = (prism.minX - x1) * dzdx + z1;
        inter2 = (prism.maxX - x1) * dzdx + z1;
        if (Math.min(inter1, inter2) > prism.maxZ || Math.max(inter1, inter2) < prism.minZ) return false;
        // project to yz plane
        inter1 = (prism.minY - y1) * dzdy + z1;
        inter2 = (prism.maxY - y1) * dzdy + z1;
        return !(Math.min(inter1, inter2) > prism.maxZ || Math.max(inter1, inter2) < prism.minZ);
    }

    private static final EnumMap<EnumFacing, Vec3d> FACE_VEC_MAPPING = Arrays.stream(EnumFacing.VALUES)
            .collect(Collectors.toMap(d -> d, d -> new Vec3d(d.getDirectionVec()),
                    (a, b) -> b, () -> new EnumMap<>(EnumFacing.class)));

    public static Vec3d getDir(EnumFacing dir) {
        return FACE_VEC_MAPPING.get(dir);
    }

}
