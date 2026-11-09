package cn.xiaozhou233.orangex.wrapper.common;

/**
 * Version-independent classification of an entity target.
 *
 * <p>The wrapper only reports what the game considers the entity to be;
 * feature code decides which kinds it wants to render.</p>
 */
public enum EntityKind {

    /**
     * A player. Use {@link EntityTarget#isLocalPlayer()} to tell the local
     * player apart from everyone else.
     */
    PLAYER,

    /**
     * Hostile mobs such as zombies, skeletons, slimes and ghasts.
     */
    MONSTER,

    /**
     * Passive creatures: animals, ambient creatures (bats), water creatures
     * and golems.
     */
    ANIMAL,

    /**
     * Villagers and wandering traders.
     */
    VILLAGER,

    /**
     * Anything else: items, projectiles, paintings, experience orbs, ...
     */
    OTHER
}
