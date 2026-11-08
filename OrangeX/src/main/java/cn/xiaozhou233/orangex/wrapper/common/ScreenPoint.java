package cn.xiaozhou233.orangex.wrapper.common;

/**
 * A world position projected into 2D GUI pixel space.
 *
 * <p>{@link #isVisible()} is {@code false} when the point is behind the
 * camera (or outside the near plane); callers must not draw such a point.</p>
 */
public final class ScreenPoint {

    private final double x;
    private final double y;
    private final double depth;
    private final boolean visible;

    public ScreenPoint(double x, double y, double depth, boolean visible) {
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.visible = visible;
    }

    public static ScreenPoint behind() {
        return new ScreenPoint(0.0D, 0.0D, -1.0D, false);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * Distance along the camera forward axis.
     */
    public double getDepth() {
        return depth;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return "ScreenPoint{x=" + x + ", y=" + y + ", depth=" + depth
                + ", visible=" + visible + "}";
    }
}
