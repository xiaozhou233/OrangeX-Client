package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import cn.xiaozhou233.orangex.wrapper.internal.ReflectionSupport;

import java.lang.reflect.Field;

/**
 * 1.8.9 implementation of {@link Player}.
 *
 * <p>The jump cooldown is {@code EntityLivingBase#jumpTicks}: vanilla sets it
 * to 10 after a jump and only allows the next jump once it reaches 0. Setting
 * it to 0 every tick removes the cooldown.</p>
 */
public final class V1_8_9Player implements Player {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String ENTITY_LIVING = "net/minecraft/entity/EntityLivingBase";

    private static final String PLAYER_FIELD = "thePlayer";
    private static final String JUMP_DELAY_FIELD = "jumpTicks";

    private final Object minecraft;
    private final Field playerField;
    private final String jumpDelayFieldName;

    private Field jumpDelayField;

    public V1_8_9Player(MappingResolver resolver, Minecraft minecraft) {
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
