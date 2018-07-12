package io.github.phantamanta44.libnine.util.math;

import net.minecraft.util.math.Vec3d;

public class Mat3d {
    
    private final double[] values;
    
    public Mat3d(double a11, double a12, double a13,
                 double a21, double a22, double a23,
                 double a31, double a32, double a33) {
        this.values = new double[] { a11, a12, a13, a21, a22, a23, a31, a32, a33 };
    }
    
    public Vec3d multiply(Vec3d vec) {
        return new Vec3d(vec.x * values[0] + vec.y * values[1] + vec.z * values[2],
                vec.x * values[3] + vec.y * values[4] + vec.z * values[5],
                vec.x * values[6] + vec.y * values[7] + vec.z * values[8]);
    }
    
}
