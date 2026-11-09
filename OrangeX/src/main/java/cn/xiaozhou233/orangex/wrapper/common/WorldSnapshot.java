package cn.xiaozhou233.orangex.wrapper.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One frame of world data: camera state plus the entity list.
 *
 * <p>Snapshots are immutable and are produced on the render thread, so the
 * partial tick is read only once and every position in this object belongs to
 * the same frame. Entries are not filtered by gameplay rules.</p>
 */
public final class WorldSnapshot {

    private final Vec3 cameraPosition;
    private final float cameraYaw;
    private final float cameraPitch;
    private final float fieldOfView;
    private final List<EntityTarget> targets;

    public WorldSnapshot(Vec3 cameraPosition,
                         float cameraYaw,
                         float cameraPitch,
                         float fieldOfView,
                         List<EntityTarget> targets) {
        this.cameraPosition = cameraPosition;
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
        this.fieldOfView = fieldOfView;
        this.targets = Collections.unmodifiableList(
                new ArrayList<EntityTarget>(targets));
    }

    public Vec3 getCameraPosition() {
        return cameraPosition;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    /**
     * Vertical field of view in degrees.
     */
    public float getFieldOfView() {
        return fieldOfView;
    }

    public List<EntityTarget> getTargets() {
        return targets;
    }

    public ScreenPoint project(Vec3 worldPoint, int screenWidth, int screenHeight) {
        return Projection.project(cameraPosition, cameraYaw, cameraPitch,
                fieldOfView, screenWidth, screenHeight, worldPoint);
    }
}
