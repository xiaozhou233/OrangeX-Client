package cn.xiaozhou233.orangex.wrapper.internal.v26_2;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import cn.xiaozhou233.orangex.wrapper.internal.ReflectionSupport;

import java.lang.reflect.Field;

/**
 * 26.2 implementation of {@link Player}.
 *
 * <p>Modern clients replaced the 1.8 {@code jumpTicks} counter with
 * {@code LivingEntity#noJumpDelay}, which is decremented once per tick and
 * blocks a new jump while positive. Zeroing it every tick removes the delay.</p>
 */
public final class V26_2Player implements Player {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String ENTITY_LIVING = "net/minecraft/world/entity/LivingEntity";

    private static final String PLAYER_FIELD = "player";
    private static final String JUMP_DELAY_FIELD = "noJumpDelay";

    private final Object minecraft;
    private final Field playerField;
    private final String jumpDelayFieldName;

    private Field jumpDelayField;

    public V26_2Player(MappingResolver resolver, Minecraft minecraft) {
        this.minecraft = minecraft.getMinecraft();

        MappingClass minecraftMapping = resolver.mapClass(MINECRAFT);
        this.playerField = resolveField(
                minecraftMapping, PLAYER_FIELD, this.minecraft.getClass());
        this.jumpDelayFieldName = fieldName(
                resolver.mapClass(ENTITY_LIVING), JUMP_DELAY_FIELD);
    }

    @Override
    public void clearJumpDelay() {
        Object localPlayer = ReflectionSupport.get(playerField, minecraft);
        if (localPlayer == null) {
            return;
        }

        // The player class is stable for the process, but the player instance
        // is not, so only the field lookup is cached.
        if (jumpDelayField == null) {
            jumpDelayField = ReflectionSupport.requireField(
                    localPlayer.getClass(), jumpDelayFieldName, ENTITY_LIVING);
        }

        ReflectionSupport.setInt(jumpDelayField, localPlayer, 0);
    }

    private static Field resolveField(MappingClass owner,
                                      String name,
                                      Class<?> runtimeOwner) {
        MappingField mapping = owner.getField(name);
        if (mapping == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + name + "' on "
                            + owner.getSourceName());
        }
        return ReflectionSupport.requireField(
                runtimeOwner, mapping.getTargetName(), owner.getSourceName());
    }

    private static String fieldName(MappingClass owner, String name) {
        MappingField mapping = owner.getField(name);
        if (mapping == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + name + "' on "
                            + owner.getSourceName());
        }
        return mapping.getTargetName();
    }
}
