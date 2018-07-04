package io.github.phantamanta44.libnine.util.math;

public class Vec2i {
    
    private final int x, y;
    
    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }

    public Vec2i add(int xAddend, int yAddend) {
        return new Vec2i(x + xAddend, y + yAddend);
    }
    
    public Vec2i add(Vec2i vec) {
        return add(vec.x, vec.y);
    }
    
    public int dot(Vec2i vec) {
        return x * vec.x + y * vec.y;
    }
    
    public int getMagnitudeSq() {
        return x * x + y * y;
    }
    
    public double getMagnitude() {
        return Math.sqrt(getMagnitudeSq());
    }
    
    public double getDistance(Vec2i vec) {
        return Math.hypot(vec.x - x, vec.y - y);
    }
    
}
