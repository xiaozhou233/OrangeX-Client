package cn.xiaozhou233.orangex.wrapper.common;

/**
 * Version-independent snapshot of one player entity that feature code can
 * render (for example an ESP target).
 */
public final class EntityTarget {

    private final String name;
    private final Vec3 position;
    private final double width;
    private final double height;
    private final float health;
    private final float maxHealth;
    private final double distance;

    public EntityTarget(String name,
                        Vec3 position,
                        double width,
                        double height,
                        float health,
                        float maxHealth,
                        double distance) {
        this.name = name;
        this.position = position;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = maxHealth;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    /**
     * Interpolated feet position of the entity.
     */
    public Vec3 getPosition() {
        return position;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    /**
     * Distance between the entity feet and the camera.
     */
    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "EntityTarget{name='" + name + "', position=" + position
                + ", health=" + health + "/" + maxHealth
                + ", distance=" + distance + "}";
    }
}
