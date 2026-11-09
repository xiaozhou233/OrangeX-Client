package cn.xiaozhou233.orangex.wrapper.common;

/**
 * Version-independent snapshot of one entity that feature code can render
 * (for example an ESP target).
 */
public final class EntityTarget {

    private final String name;
    private final Vec3 position;
    private final double width;
    private final double height;
    private final float health;
    private final float maxHealth;
    private final double distance;
    private final EntityKind kind;
    private final boolean localPlayer;
    private final boolean invisible;

    public EntityTarget(String name,
                        Vec3 position,
                        double width,
                        double height,
                        float health,
                        float maxHealth,
                        double distance,
                        EntityKind kind,
                        boolean localPlayer,
                        boolean invisible) {
        this.name = name;
        this.position = position;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = maxHealth;
        this.distance = distance;
        this.kind = kind;
        this.localPlayer = localPlayer;
        this.invisible = invisible;
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

    public EntityKind getKind() {
        return kind;
    }

    /**
     * {@code true} when this target is the entity the camera belongs to.
     */
    public boolean isLocalPlayer() {
        return localPlayer;
    }

    /**
     * {@code true} when the game reports the entity as invisible (invisibility
     * potion, spectator, invisible armor stands, ...).
     */
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public String toString() {
        return "EntityTarget{name='" + name + "', kind=" + kind
                + ", localPlayer=" + localPlayer
                + ", invisible=" + invisible
                + ", position=" + position
                + ", health=" + health + "/" + maxHealth
                + ", distance=" + distance + "}";
    }
}
