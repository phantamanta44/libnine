package io.github.phantamanta44.libnine.util.helper;

public class MathUtils {
    
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
    
}
