package cn.xiaozhou233.orangex.wrapper.common;

/**
 * Camera-independent perspective projection.
 *
 * <p>Minecraft treats the perspective FOV as a <em>vertical</em> angle in
 * degrees and uses a conventional right-handed view: yaw {@code 0} looks
 * towards {@code +Z}, positive yaw turns towards {@code -X}, and positive
 * pitch looks down. Both the 1.8.9 and the modern clients share these
 * conventions, so the same math works on every version.</p>
 */
public final class Projection {

    /**
     * Points closer than this are treated as behind the camera. It matches the
     * near plane Minecraft uses for its perspective projection.
     */
    private static final double NEAR_PLANE = 0.05D;

    private static final float DEFAULT_FOV = 70.0F;

    public static ScreenPoint project(Vec3 camera,
                                      float yawDegrees,
                                      float pitchDegrees,
                                      float fovDegrees,
                                      int screenWidth,
                                      int screenHeight,
                                      Vec3 point) {
        if (camera == null || point == null
                || screenWidth <= 0 || screenHeight <= 0) {
            return ScreenPoint.behind();
        }

        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);

        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);

        // Camera basis (camera looks along -forward, forwards is +Z at yaw 0).
        double forwardX = -sinYaw * cosPitch;
        double forwardY = -sinPitch;
        double forwardZ = cosYaw * cosPitch;

        double rightX = -cosYaw;
        double rightZ = -sinYaw;

        double upX = -sinYaw * sinPitch;
        double upY = cosPitch;
        double upZ = cosYaw * sinPitch;

        double dx = point.getX() - camera.getX();
        double dy = point.getY() - camera.getY();
        double dz = point.getZ() - camera.getZ();

        double depth = dx * forwardX + dy * forwardY + dz * forwardZ;
        if (depth <= NEAR_PLANE) {
            return ScreenPoint.behind();
        }

        double x = dx * rightX + dz * rightZ;
        double y = dx * upX + dy * upY + dz * upZ;

        float fov = fovDegrees > 1.0F ? fovDegrees : DEFAULT_FOV;
        double tanHalfFov = Math.tan(Math.toRadians(fov) * 0.5D);
        double aspect = (double) screenWidth / (double) screenHeight;

        double ndcX = x / (depth * tanHalfFov * aspect);
        double ndcY = y / (depth * tanHalfFov);

        double screenX = (ndcX + 1.0D) * 0.5D * screenWidth;
        double screenY = (1.0D - ndcY) * 0.5D * screenHeight;

        return new ScreenPoint(screenX, screenY, depth, true);
    }

    private Projection() {
    }
}
