package cn.xiaozhou233.orangex.wrapper.common;

import java.util.Objects;

/**
 * Immutable 3D position / vector used by the wrapper API.
 */
public final class Vec3 {

    public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);

    private final double x;
    private final double y;
    private final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vec3 add(Vec3 other) {
        Objects.requireNonNull(other, "other");
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 subtract(Vec3 other) {
        Objects.requireNonNull(other, "other");
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distanceSquared(Vec3 other) {
        Objects.requireNonNull(other, "other");
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceTo(Vec3 other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Vec3)) {
            return false;
        }
        Vec3 other = (Vec3) object;
        return Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
