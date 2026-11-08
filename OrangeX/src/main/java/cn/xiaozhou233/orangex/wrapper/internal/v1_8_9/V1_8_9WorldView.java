package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
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
 * 1.8.9 implementation of {@link WorldView}.
 *
 * <p>All names below are MCP (named) names and are translated through the
 * active {@link MappingResolver}. The camera math mirrors
 * {@code EntityRenderer#orientCamera}: the eye position of the render view
 * entity plus an optional third-person offset along the camera forward
 * vector.</p>
 */
public final class V1_8_9WorldView implements WorldView {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String GAME_SETTINGS = "net/minecraft/client/settings/GameSettings";
    private static final String TIMER = "net/minecraft/util/Timer";
    private static final String WORLD = "net/minecraft/world/World";
    private static final String ENTITY = "net/minecraft/entity/Entity";
    private static final String ENTITY_LIVING = "net/minecraft/entity/EntityLivingBase";
    private static final String ENTITY_PLAYER = "net/minecraft/entity/player/EntityPlayer";
    private static final String ENTITY_RENDERER = "net/minecraft/client/renderer/EntityRenderer";

    private static final float DEFAULT_FOV = 70.0F;
    private static final float DEFAULT_THIRD_PERSON_DISTANCE = 4.0F;
    private static final double FIRST_PERSON_OFFSET = 0.1D;

    private final MappingResolver resolver;
    private final Object minecraft;

    private final Field worldField;
    private final Field playerField;
    private final Field viewEntityField;
    private final Field timerField;
    private final Field gameSettingsField;
    private final Field entityRendererField;

    private final String partialTicksName;
    private final String playerEntitiesName;
    private final String thirdPersonViewName;
    private final String fovSettingName;
    private final String fovModifierMethodName;
    private final String thirdPersonDistanceName;
    private final String thirdPersonDistanceTempName;

    private Field partialTicksField;
    private Field playerEntitiesField;
    private Field thirdPersonViewField;
    private Field fovSettingField;
    private Field thirdPersonDistanceField;
    private Field thirdPersonDistanceTempField;
    private Method fovModifierMethod;
    private boolean optionalCameraFieldsResolved;

    private final Map<Class<?>, EntityAccess> accessCache =
            new HashMap<Class<?>, EntityAccess>();

    public V1_8_9WorldView(MappingResolver resolver, Minecraft minecraft) {
        this.resolver = resolver;
        this.minecraft = minecraft.getMinecraft();
        Class<?> minecraftClass = this.minecraft.getClass();

        MappingClass minecraftMapping = resolver.mapClass(MINECRAFT);
        this.worldField = resolveField(minecraftMapping, "theWorld", minecraftClass);
        this.playerField = resolveField(minecraftMapping, "thePlayer", minecraftClass);
        this.viewEntityField = resolveField(minecraftMapping, "renderViewEntity", minecraftClass);
        this.timerField = resolveField(minecraftMapping, "timer", minecraftClass);
        this.gameSettingsField = resolveField(minecraftMapping, "gameSettings", minecraftClass);
        this.entityRendererField = resolveField(minecraftMapping, "entityRenderer", minecraftClass);

        this.partialTicksName = fieldName(TIMER, "renderPartialTicks");
        this.playerEntitiesName = fieldName(WORLD, "playerEntities");
        this.thirdPersonViewName = fieldName(GAME_SETTINGS, "thirdPersonView");
        this.fovSettingName = fieldName(GAME_SETTINGS, "fovSetting");
        this.fovModifierMethodName = methodName(
                ENTITY_RENDERER, "getFOVModifier", "(FZ)F");
        this.thirdPersonDistanceName = optionalFieldName(
                ENTITY_RENDERER, "thirdPersonDistance");
        this.thirdPersonDistanceTempName = optionalFieldName(
                ENTITY_RENDERER, "thirdPersonDistanceTemp");
    }

    @Override
    public WorldSnapshot snapshot() {
        Object world = ReflectionSupport.get(worldField, minecraft);
        Object viewEntity = ReflectionSupport.get(viewEntityField, minecraft);
        Object localPlayer = ReflectionSupport.get(playerField, minecraft);

        if (world == null || viewEntity == null) {
            return null;
        }

        float partialTicks = partialTicks();
        EntityAccess view = accessFor(viewEntity.getClass());

        double eyeX = interpolate(view.posX, view.lastTickPosX, viewEntity, partialTicks);
        double eyeY = interpolate(view.posY, view.lastTickPosY, viewEntity, partialTicks)
                + invokeFloat(view.eyeHeight, viewEntity);
        double eyeZ = interpolate(view.posZ, view.lastTickPosZ, viewEntity, partialTicks);

        float yaw = interpolateAngle(view.rotationYaw, view.prevRotationYaw,
                viewEntity, partialTicks);
        float pitch = interpolateAngle(view.rotationPitch, view.prevRotationPitch,
                viewEntity, partialTicks);

        int thirdPerson = thirdPersonView();
        float cameraYaw = yaw;
        float cameraPitch = pitch;

        if (thirdPerson == 2) {
            // Front view: the camera sits in front of the player and looks
            // back, so both axes are mirrored.
            cameraYaw = yaw + 180.0F;
            cameraPitch = -pitch;
        }

        // Third person moves the camera back by the configured distance;
        // first person sits 0.1 blocks behind the eye (EntityRenderer).
        double cameraOffset = thirdPerson > 0
                ? thirdPersonDistance(partialTicks)
                : FIRST_PERSON_OFFSET;

        double yawRadians = Math.toRadians(cameraYaw);
        double pitchRadians = Math.toRadians(cameraPitch);

        double forwardX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = -Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        double cameraX = eyeX - forwardX * cameraOffset;
        double cameraY = eyeY - forwardY * cameraOffset;
        double cameraZ = eyeZ - forwardZ * cameraOffset;

        float fov = fieldOfView(partialTicks);
        List<EntityTarget> targets = collectTargets(
                world, localPlayer, cameraX, cameraY, cameraZ, partialTicks);

        return new WorldSnapshot(new Vec3(cameraX, cameraY, cameraZ),
                cameraYaw, cameraPitch, fov, targets);
    }

    private List<EntityTarget> collectTargets(Object world,
                                              Object localPlayer,
                                              double cameraX,
                                              double cameraY,
                                              double cameraZ,
                                              float partialTicks) {
        List<EntityTarget> targets = new ArrayList<EntityTarget>();

        Field entitiesField = playerEntitiesField(world.getClass());
        if (entitiesField == null) {
            return targets;
        }

        Object value = ReflectionSupport.get(entitiesField, world);
        if (!(value instanceof List)) {
            return targets;
        }

        for (Object entity : (List<?>) value) {
            if (entity == null || entity == localPlayer) {
                continue;
            }

            EntityAccess access = accessFor(entity.getClass());

            if (access.isAlive != null && !invokeBoolean(access.isAlive, entity)) {
                continue;
            }
            if (access.isInvisible != null && invokeBoolean(access.isInvisible, entity)) {
                continue;
            }
            if (access.isSpectator != null && invokeBoolean(access.isSpectator, entity)) {
                continue;
            }

            float health = invokeFloat(access.getHealth, entity);
            if (health <= 0.0F) {
                continue;
            }

            double x = interpolate(access.posX, access.lastTickPosX, entity, partialTicks);
            double y = interpolate(access.posY, access.lastTickPosY, entity, partialTicks);
            double z = interpolate(access.posZ, access.lastTickPosZ, entity, partialTicks);

            double width = access.width == null ? 0.6D
                    : ReflectionSupport.getFloat(access.width, entity);
            double height = access.height == null ? 1.8D
                    : ReflectionSupport.getFloat(access.height, entity);

            double distance = distance(cameraX, cameraY, cameraZ, x, y, z);

            targets.add(new EntityTarget(
                    String.valueOf(ReflectionSupport.invoke(access.getName, entity)),
                    new Vec3(x, y, z),
                    width,
                    height,
                    health,
                    invokeFloat(access.getMaxHealth, entity),
                    distance));
        }

        return targets;
    }

    private float partialTicks() {
        Object timer = ReflectionSupport.get(timerField, minecraft);
        if (timer == null) {
            return 0.0F;
        }

        if (partialTicksField == null) {
            partialTicksField = ReflectionSupport.findField(
                    timer.getClass(), partialTicksName);
        }

        return partialTicksField == null
                ? 0.0F
                : ReflectionSupport.getFloat(partialTicksField, timer);
    }

    private int thirdPersonView() {
        Object settings = ReflectionSupport.get(gameSettingsField, minecraft);
        if (settings == null) {
            return 0;
        }

        if (thirdPersonViewField == null) {
            thirdPersonViewField = ReflectionSupport.findField(
                    settings.getClass(), thirdPersonViewName);
        }
        if (thirdPersonViewField == null) {
            return 0;
        }

        // 1.8.9 stores an int (0/1/2); some 1.8 builds use a boolean flag.
        if (thirdPersonViewField.getType() == boolean.class) {
            return ReflectionSupport.getBoolean(thirdPersonViewField, settings) ? 1 : 0;
        }
        return ReflectionSupport.getInt(thirdPersonViewField, settings);
    }

    private float fieldOfView(float partialTicks) {
        Object renderer = ReflectionSupport.get(entityRendererField, minecraft);
        if (renderer != null && fovModifierMethod == null) {
            fovModifierMethod = ReflectionSupport.findMethod(
                    renderer.getClass(), fovModifierMethodName,
                    float.class, float.class, boolean.class);
        }

        if (fovModifierMethod != null) {
            return ((Number) ReflectionSupport.invoke(
                    fovModifierMethod, renderer, partialTicks, Boolean.TRUE))
                    .floatValue();
        }

        Object settings = ReflectionSupport.get(gameSettingsField, minecraft);
        if (settings != null) {
            if (fovSettingField == null) {
                fovSettingField = ReflectionSupport.findField(
                        settings.getClass(), fovSettingName);
            }
            if (fovSettingField != null) {
                return ReflectionSupport.getFloat(fovSettingField, settings);
            }
        }

        return DEFAULT_FOV;
    }

    private double thirdPersonDistance(float partialTicks) {
        Object renderer = ReflectionSupport.get(entityRendererField, minecraft);
        if (renderer == null || !resolveOptionalCameraFields(renderer.getClass())) {
            return DEFAULT_THIRD_PERSON_DISTANCE;
        }

        float previous = thirdPersonDistanceTempField == null
                ? DEFAULT_THIRD_PERSON_DISTANCE
                : ReflectionSupport.getFloat(thirdPersonDistanceTempField, renderer);
        float current = thirdPersonDistanceField == null
                ? DEFAULT_THIRD_PERSON_DISTANCE
                : ReflectionSupport.getFloat(thirdPersonDistanceField, renderer);

        return previous + (current - previous) * partialTicks;
    }

    private boolean resolveOptionalCameraFields(Class<?> rendererClass) {
        if (optionalCameraFieldsResolved) {
            return true;
        }
        optionalCameraFieldsResolved = true;

        if (thirdPersonDistanceName != null) {
            thirdPersonDistanceField = ReflectionSupport.findField(
                    rendererClass, thirdPersonDistanceName);
        }
        if (thirdPersonDistanceTempName != null) {
            thirdPersonDistanceTempField = ReflectionSupport.findField(
                    rendererClass, thirdPersonDistanceTempName);
        }

        return thirdPersonDistanceField != null
                || thirdPersonDistanceTempField != null;
    }

    private Field playerEntitiesField(Class<?> worldClass) {
        if (playerEntitiesField == null) {
            playerEntitiesField = ReflectionSupport.findField(
                    worldClass, playerEntitiesName);
        }
        return playerEntitiesField;
    }

    private EntityAccess accessFor(Class<?> entityClass) {
        EntityAccess access = accessCache.get(entityClass);
        if (access == null) {
            access = new EntityAccess(entityClass);
            accessCache.put(entityClass, access);
        }
        return access;
    }

    private Field resolveField(MappingClass owner, String name, Class<?> runtimeOwner) {
        MappingField mapping = owner.getField(name);
        if (mapping == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + name + "' on " + owner.getSourceName());
        }
        return ReflectionSupport.requireField(
                runtimeOwner, mapping.getTargetName(), owner.getSourceName());
    }

    private String fieldName(String ownerClass, String name) {
        MappingClass owner = resolver.mapClass(ownerClass);
        MappingField mapping = owner.getField(name);
        if (mapping == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + name + "' on " + ownerClass);
        }
        return mapping.getTargetName();
    }

    private String optionalFieldName(String ownerClass, String name) {
        MappingClass owner = resolver.mapClass(ownerClass);
        MappingField mapping = owner.getField(name);
        return mapping == null ? null : mapping.getTargetName();
    }

    private String methodName(String ownerClass, String name, String descriptor) {
        MappingClass owner = resolver.mapClass(ownerClass);
        MappingMethod mapping = owner.getMethod(name, descriptor);
        if (mapping == null) {
            throw new IllegalStateException(
                    "No mapping for method '" + name + descriptor + "' on " + ownerClass);
        }
        return mapping.getTargetName();
    }

    private String optionalMethodName(String ownerClass, String name, String descriptor) {
        MappingClass owner = resolver.mapClass(ownerClass);
        MappingMethod mapping = owner.getMethod(name, descriptor);
        return mapping == null ? null : mapping.getTargetName();
    }

    private static double interpolate(Field current, Field previous,
                                      Object entity, float partialTicks) {
        double previousValue = ReflectionSupport.getDouble(previous, entity);
        double currentValue = ReflectionSupport.getDouble(current, entity);
        return previousValue + (currentValue - previousValue) * partialTicks;
    }

    private static float interpolateAngle(Field current, Field previous,
                                          Object entity, float partialTicks) {
        float previousValue = ReflectionSupport.getFloat(previous, entity);
        float currentValue = ReflectionSupport.getFloat(current, entity);
        return previousValue + (currentValue - previousValue) * partialTicks;
    }

    private static float invokeFloat(Method method, Object target) {
        return ((Number) ReflectionSupport.invoke(method, target)).floatValue();
    }

    private static boolean invokeBoolean(Method method, Object target) {
        return ((Boolean) ReflectionSupport.invoke(method, target)).booleanValue();
    }

    private static double distance(double x1, double y1, double z1,
                                   double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Per-class reflective member cache for entities. Resolving methods on
     * every frame for every player would be needlessly slow.
     */
    private final class EntityAccess {

        private final Field posX;
        private final Field posY;
        private final Field posZ;
        private final Field lastTickPosX;
        private final Field lastTickPosY;
        private final Field lastTickPosZ;
        private final Field width;
        private final Field height;
        private final Field rotationYaw;
        private final Field rotationPitch;
        private final Field prevRotationYaw;
        private final Field prevRotationPitch;
        private final Method eyeHeight;
        private final Method getName;
        private final Method getHealth;
        private final Method getMaxHealth;
        private final Method isAlive;
        private final Method isInvisible;
        private final Method isSpectator;

        private EntityAccess(Class<?> entityClass) {
            MappingClass entity = resolver.mapClass(ENTITY);
            MappingClass living = resolver.mapClass(ENTITY_LIVING);
            MappingClass player = resolver.mapClass(ENTITY_PLAYER);

            this.posX = mappedField(entity, "posX", entityClass);
            this.posY = mappedField(entity, "posY", entityClass);
            this.posZ = mappedField(entity, "posZ", entityClass);
            this.lastTickPosX = mappedField(entity, "lastTickPosX", entityClass);
            this.lastTickPosY = mappedField(entity, "lastTickPosY", entityClass);
            this.lastTickPosZ = mappedField(entity, "lastTickPosZ", entityClass);
            this.rotationYaw = mappedField(entity, "rotationYaw", entityClass);
            this.rotationPitch = mappedField(entity, "rotationPitch", entityClass);
            this.prevRotationYaw = mappedField(entity, "prevRotationYaw", entityClass);
            this.prevRotationPitch = mappedField(entity, "prevRotationPitch", entityClass);
            this.width = mappedField(entity, "width", entityClass);
            this.height = mappedField(entity, "height", entityClass);

            this.eyeHeight = mappedMethod(entity, "getEyeHeight", "()F",
                    float.class, entityClass);
            this.isAlive = mappedMethod(entity, "isEntityAlive", "()Z",
                    boolean.class, entityClass);
            this.isInvisible = mappedMethod(entity, "isInvisible", "()Z",
                    boolean.class, entityClass);
            this.getHealth = mappedMethod(living, "getHealth", "()F",
                    float.class, entityClass);
            this.getMaxHealth = mappedMethod(living, "getMaxHealth", "()F",
                    float.class, entityClass);
            this.getName = mappedMethod(player, "getName", "()Ljava/lang/String;",
                    String.class, entityClass);
            this.isSpectator = optionalMappedMethod(player, "isSpectator", "()Z",
                    boolean.class, entityClass);
        }

        private Field mappedField(MappingClass owner, String name, Class<?> type) {
            MappingField mapping = owner.getField(name);
            if (mapping == null) {
                throw new IllegalStateException("No mapping for field '" + name
                        + "' on " + owner.getSourceName());
            }
            return ReflectionSupport.requireField(
                    type, mapping.getTargetName(), owner.getSourceName());
        }

        private Method mappedMethod(MappingClass owner, String name,
                                    String descriptor, Class<?> returnType,
                                    Class<?> type) {
            String target = methodName(owner.getSourceName(), name, descriptor);
            return ReflectionSupport.requireMethod(type, target, returnType,
                    owner.getSourceName(), new Class<?>[0]);
        }

        private Method optionalMappedMethod(MappingClass owner, String name,
                                            String descriptor, Class<?> returnType,
                                            Class<?> type) {
            String target = optionalMethodName(owner.getSourceName(), name, descriptor);
            if (target == null) {
                return null;
            }
            return ReflectionSupport.findMethod(type, target, returnType,
                    new Class<?>[0]);
        }
    }
}
