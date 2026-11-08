package cn.xiaozhou233.orangex.wrapper.internal.v26_2;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
import cn.xiaozhou233.orangex.wrapper.common.EntityTarget;
import cn.xiaozhou233.orangex.wrapper.common.Vec3;
import cn.xiaozhou233.orangex.wrapper.common.WorldSnapshot;
import cn.xiaozhou233.orangex.wrapper.internal.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.2 implementation of {@link WorldView}.
 *
 * <p>The modern client already exposes an interpolated camera
 * ({@code GameRenderer#mainCamera}) and its FOV, so no third-person math is
 * needed here. Member names are the Mojang names the runtime uses.</p>
 */
public final class V26_2WorldView implements WorldView {

    private static final float DEFAULT_FOV = 70.0F;

    private final Object minecraft;

    private final Field levelField;
    private final Field playerField;
    private final Field gameRendererField;

    private Method deltaTrackerMethod;
    private Method deltaPartialTickMethod;
    private Method mainCameraMethod;
    private Method cameraPositionMethod;
    private Method cameraXRotMethod;
    private Method cameraYRotMethod;
    private Method cameraFovMethod;

    private final Map<Class<?>, EntityAccess> accessCache =
            new HashMap<Class<?>, EntityAccess>();
    private final Map<Class<?>, Field[]> vectorFields =
            new HashMap<Class<?>, Field[]>();

    public V26_2WorldView(Minecraft minecraft) {
        this.minecraft = minecraft.getMinecraft();
        Class<?> minecraftClass = this.minecraft.getClass();

        this.levelField = ReflectionSupport.requireField(
                minecraftClass, "level", "net.minecraft.client.Minecraft");
        this.playerField = ReflectionSupport.requireField(
                minecraftClass, "player", "net.minecraft.client.Minecraft");
        this.gameRendererField = ReflectionSupport.requireField(
                minecraftClass, "gameRenderer", "net.minecraft.client.Minecraft");
    }

    @Override
    public WorldSnapshot snapshot() {
        Object level = ReflectionSupport.get(levelField, minecraft);
        Object localPlayer = ReflectionSupport.get(playerField, minecraft);
        Object gameRenderer = ReflectionSupport.get(gameRendererField, minecraft);

        if (level == null || gameRenderer == null) {
            return null;
        }

        float partialTicks = partialTicks();
        Object camera = camera(gameRenderer);
        if (camera == null) {
            return null;
        }

        Vec3 cameraPosition = readVec3(
                ReflectionSupport.invoke(cameraPositionMethod, camera));
        float yaw = invokeFloat(cameraYRotMethod, camera);
        float pitch = invokeFloat(cameraXRotMethod, camera);
        float fov = invokeFloat(cameraFovMethod, camera);

        List<EntityTarget> targets = collectTargets(
                level, localPlayer, cameraPosition, partialTicks);

        return new WorldSnapshot(cameraPosition, yaw, pitch,
                fov > 1.0F ? fov : DEFAULT_FOV, targets);
    }

    private Object camera(Object gameRenderer) {
        if (mainCameraMethod == null) {
            mainCameraMethod = ReflectionSupport.findMethod(
                    gameRenderer.getClass(), "mainCamera", null);
        }
        if (mainCameraMethod == null) {
            return null;
        }

        Object camera = ReflectionSupport.invoke(mainCameraMethod, gameRenderer);
        if (camera == null) {
            return null;
        }

        Class<?> cameraClass = camera.getClass();
        if (cameraPositionMethod == null) {
            cameraPositionMethod = ReflectionSupport.requireMethod(
                    cameraClass, "position", null, "net.minecraft.client.Camera");
            cameraXRotMethod = ReflectionSupport.requireMethod(
                    cameraClass, "xRot", float.class, "net.minecraft.client.Camera");
            cameraYRotMethod = ReflectionSupport.requireMethod(
                    cameraClass, "yRot", float.class, "net.minecraft.client.Camera");
            cameraFovMethod = ReflectionSupport.requireMethod(
                    cameraClass, "getFov", float.class, "net.minecraft.client.Camera");
        }

        return camera;
    }

    private List<EntityTarget> collectTargets(Object level,
                                              Object localPlayer,
                                              Vec3 cameraPosition,
                                              float partialTicks) {
        List<EntityTarget> targets = new ArrayList<EntityTarget>();

        Method playersMethod = ReflectionSupport.findMethod(
                level.getClass(), "players", null);
        if (playersMethod == null) {
            return targets;
        }

        Object value = ReflectionSupport.invoke(playersMethod, level);
        if (!(value instanceof Iterable)) {
            return targets;
        }

        for (Object entity : (Iterable<?>) value) {
            if (entity == null || entity == localPlayer) {
                continue;
            }

            EntityAccess access = accessFor(entity.getClass());

            if (!invokeBoolean(access.isAlive, entity)) {
                continue;
            }
            if (invokeBoolean(access.isInvisible, entity)) {
                continue;
            }
            if (access.isSpectator != null && invokeBoolean(access.isSpectator, entity)) {
                continue;
            }

            float health = invokeFloat(access.getHealth, entity);
            if (health <= 0.0F) {
                continue;
            }

            Vec3 position = readVec3(ReflectionSupport.invoke(
                    access.getPosition, entity, Float.valueOf(partialTicks)));
            if (position == null) {
                continue;
            }

            targets.add(new EntityTarget(
                    invokeString(access.getName, entity),
                    position,
                    invokeFloat(access.getBbWidth, entity),
                    invokeFloat(access.getBbHeight, entity),
                    health,
                    invokeFloat(access.getMaxHealth, entity),
                    distance(cameraPosition, position)));
        }

        return targets;
    }

    private float partialTicks() {
        if (deltaTrackerMethod == null) {
            deltaTrackerMethod = ReflectionSupport.findMethod(
                    minecraft.getClass(), "getDeltaTracker", null);
        }
        if (deltaTrackerMethod == null) {
            return 0.0F;
        }

        Object tracker = ReflectionSupport.invoke(deltaTrackerMethod, minecraft);
        if (tracker == null) {
            return 0.0F;
        }

        if (deltaPartialTickMethod == null) {
            deltaPartialTickMethod = ReflectionSupport.findMethod(
                    tracker.getClass(), "getGameTimeDeltaPartialTick", null,
                    boolean.class);
        }
        if (deltaPartialTickMethod == null) {
            return 0.0F;
        }

        return invokeFloat(deltaPartialTickMethod, tracker, Boolean.FALSE);
    }

    private EntityAccess accessFor(Class<?> entityClass) {
        EntityAccess access = accessCache.get(entityClass);
        if (access == null) {
            access = new EntityAccess(entityClass);
            accessCache.put(entityClass, access);
        }
        return access;
    }

    private Vec3 readVec3(Object nativeVec) {
        if (nativeVec == null) {
            return null;
        }

        Class<?> type = nativeVec.getClass();
        Field[] fields = vectorFields.get(type);
        if (fields == null) {
            fields = new Field[]{
                    ReflectionSupport.requireField(type, "x", type.getName()),
                    ReflectionSupport.requireField(type, "y", type.getName()),
                    ReflectionSupport.requireField(type, "z", type.getName())
            };
            vectorFields.put(type, fields);
        }

        return new Vec3(
                ReflectionSupport.getDouble(fields[0], nativeVec),
                ReflectionSupport.getDouble(fields[1], nativeVec),
                ReflectionSupport.getDouble(fields[2], nativeVec));
    }

    private static String invokeString(Method method, Object target) {
        Object value = ReflectionSupport.invoke(method, target);
        if (value == null) {
            return "";
        }

        Method getString = ReflectionSupport.findMethod(
                value.getClass(), "getString", String.class);
        if (getString == null) {
            return String.valueOf(value);
        }

        Object text = ReflectionSupport.invoke(getString, value);
        return text == null ? "" : String.valueOf(text);
    }

    private static float invokeFloat(Method method, Object target, Object... arguments) {
        return ((Number) ReflectionSupport.invoke(method, target, arguments))
                .floatValue();
    }

    private static boolean invokeBoolean(Method method, Object target) {
        return ((Boolean) ReflectionSupport.invoke(method, target)).booleanValue();
    }

    private static double distance(Vec3 first, Vec3 second) {
        return first.distanceTo(second);
    }

    /**
     * Per-class reflective member cache for entities.
     */
    private static final class EntityAccess {

        private final Method getPosition;
        private final Method getName;
        private final Method getHealth;
        private final Method getMaxHealth;
        private final Method getBbWidth;
        private final Method getBbHeight;
        private final Method isAlive;
        private final Method isInvisible;
        private final Method isSpectator;

        private EntityAccess(Class<?> entityClass) {
            this.getPosition = ReflectionSupport.requireMethod(
                    entityClass, "getPosition", null, entityClass.getName(),
                    float.class);
            this.getName = ReflectionSupport.requireMethod(
                    entityClass, "getName", null, entityClass.getName());
            this.getHealth = ReflectionSupport.requireMethod(
                    entityClass, "getHealth", float.class, entityClass.getName());
            this.getMaxHealth = ReflectionSupport.requireMethod(
                    entityClass, "getMaxHealth", float.class, entityClass.getName());
            this.getBbWidth = ReflectionSupport.requireMethod(
                    entityClass, "getBbWidth", float.class, entityClass.getName());
            this.getBbHeight = ReflectionSupport.requireMethod(
                    entityClass, "getBbHeight", float.class, entityClass.getName());
            this.isAlive = ReflectionSupport.requireMethod(
                    entityClass, "isAlive", boolean.class, entityClass.getName());
            this.isInvisible = ReflectionSupport.requireMethod(
                    entityClass, "isInvisible", boolean.class, entityClass.getName());
            this.isSpectator = ReflectionSupport.findMethod(
                    entityClass, "isSpectator", boolean.class);
        }
    }
}
