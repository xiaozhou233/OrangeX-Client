package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
import cn.xiaozhou233.orangex.wrapper.common.EntityKind;
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
    private static final String ENTITY_MONSTER = "net/minecraft/entity/monster/IMob";
    private static final String ENTITY_ANIMAL = "net/minecraft/entity/passive/EntityAnimal";
    private static final String ENTITY_AMBIENT = "net/minecraft/entity/passive/EntityAmbientCreature";
    private static final String ENTITY_WATER = "net/minecraft/entity/passive/EntityWaterMob";
    private static final String ENTITY_GOLEM = "net/minecraft/entity/monster/EntityGolem";
    private static final String ENTITY_VILLAGER = "net/minecraft/entity/passive/EntityVillager";
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
    private final String loadedEntitiesName;
    private final String thirdPersonViewName;
    private final String fovSettingName;
    private final String fovModifierMethodName;
    private final String thirdPersonDistanceName;
    private final String thirdPersonDistanceTempName;

    private final String[] playerClassNames;
    private final String[] monsterClassNames;
    private final String[] animalClassNames;
    private final String[] villagerClassNames;

    private Field partialTicksField;
    private boolean partialTicksWarningLogged;
    private Field loadedEntitiesField;
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
        this.loadedEntitiesName = fieldName(WORLD, "loadedEntityList");
        this.thirdPersonViewName = fieldName(GAME_SETTINGS, "thirdPersonView");
        this.fovSettingName = fieldName(GAME_SETTINGS, "fovSetting");
        this.fovModifierMethodName = methodName(
                ENTITY_RENDERER, "getFOVModifier", "(FZ)F");
        this.thirdPersonDistanceName = optionalFieldName(
                ENTITY_RENDERER, "thirdPersonDistance");
        this.thirdPersonDistanceTempName = optionalFieldName(
                ENTITY_RENDERER, "thirdPersonDistanceTemp");

        this.playerClassNames = classNames(ENTITY_PLAYER);
        this.monsterClassNames = classNames(ENTITY_MONSTER);
        this.animalClassNames = classNames(
                ENTITY_ANIMAL, ENTITY_AMBIENT, ENTITY_WATER, ENTITY_GOLEM);
        this.villagerClassNames = classNames(ENTITY_VILLAGER);
    }

    @Override
    public WorldSnapshot snapshot(float partialTicks) {
        Object world = ReflectionSupport.get(worldField, minecraft);
        Object viewEntity = ReflectionSupport.get(viewEntityField, minecraft);
        Object localPlayer = ReflectionSupport.get(playerField, minecraft);

        if (world == null || viewEntity == null) {
            return null;
        }

        // The render hook hands us the exact value the frame was rendered
        // with. Only fall back to the timer when the caller has none, because a
        // partial tick of 0 (or a stale one) makes every interpolated position
        // advance in 20 Hz steps while the world renders smoothly.
        float frameTicks = WorldView.isUsablePartialTick(partialTicks)
                ? partialTicks : partialTicks();
        EntityAccess view = accessFor(viewEntity.getClass());

        double eyeX = interpolate(view.posX, view.lastTickPosX, viewEntity, frameTicks);
        double eyeY = interpolate(view.posY, view.lastTickPosY, viewEntity, frameTicks)
                + invokeFloat(view.eyeHeight, viewEntity);
        double eyeZ = interpolate(view.posZ, view.lastTickPosZ, viewEntity, frameTicks);

        float yaw = interpolateAngle(view.rotationYaw, view.prevRotationYaw,
                viewEntity, frameTicks);
        float pitch = interpolateAngle(view.rotationPitch, view.prevRotationPitch,
                viewEntity, frameTicks);

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
                ? thirdPersonDistance(frameTicks)
                : FIRST_PERSON_OFFSET;

        double yawRadians = Math.toRadians(cameraYaw);
        double pitchRadians = Math.toRadians(cameraPitch);

        double forwardX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = -Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        double cameraX = eyeX - forwardX * cameraOffset;
        double cameraY = eyeY - forwardY * cameraOffset;
        double cameraZ = eyeZ - forwardZ * cameraOffset;

        float fov = fieldOfView(frameTicks);
        List<EntityTarget> targets = collectTargets(
                world, localPlayer, cameraX, cameraY, cameraZ, frameTicks);

        return new WorldSnapshot(new Vec3(cameraX, cameraY, cameraZ),
                cameraYaw, cameraPitch, fov, targets);
    }

    /**
     * Captures every entity in the loaded entity list. No gameplay filtering
     * happens here: feature code (for example the ESP module) decides which
     * kinds it wants to show.
     */
    private List<EntityTarget> collectTargets(Object world,
                                              Object localPlayer,
                                              double cameraX,
                                              double cameraY,
                                              double cameraZ,
                                              float partialTicks) {
        List<EntityTarget> targets = new ArrayList<EntityTarget>();

        Field entitiesField = loadedEntitiesField(world.getClass());
        if (entitiesField == null) {
            return targets;
        }

        Object value = ReflectionSupport.get(entitiesField, world);
        if (!(value instanceof List)) {
            return targets;
        }

        for (Object entity : (List<?>) value) {
            if (entity == null) {
                continue;
            }

            EntityAccess access = accessFor(entity.getClass());

            double x = interpolate(access.posX, access.lastTickPosX, entity, partialTicks);
            double y = interpolate(access.posY, access.lastTickPosY, entity, partialTicks);
            double z = interpolate(access.posZ, access.lastTickPosZ, entity, partialTicks);

            double width = access.width == null ? 0.6D
                    : ReflectionSupport.getFloat(access.width, entity);
            double height = access.height == null ? 1.8D
                    : ReflectionSupport.getFloat(access.height, entity);

            double distance = distance(cameraX, cameraY, cameraZ, x, y, z);
            boolean local = entity == localPlayer;

            targets.add(new EntityTarget(
                    String.valueOf(ReflectionSupport.invoke(access.getName, entity)),
                    new Vec3(x, y, z),
                    width,
                    height,
                    access.getHealth == null ? 0.0F : invokeFloat(access.getHealth, entity),
                    access.getMaxHealth == null ? 0.0F : invokeFloat(access.getMaxHealth, entity),
                    distance,
                    classify(entity.getClass()),
                    local,
                    invokeBoolean(access.isInvisible, entity)));
        }

        return targets;
    }

    private EntityKind classify(Class<?> entityClass) {
        if (matches(entityClass, playerClassNames)) {
            return EntityKind.PLAYER;
        }
        if (matches(entityClass, villagerClassNames)) {
            return EntityKind.VILLAGER;
        }
        if (matches(entityClass, monsterClassNames)) {
            return EntityKind.MONSTER;
        }
        if (matches(entityClass, animalClassNames)) {
            return EntityKind.ANIMAL;
        }
        return EntityKind.OTHER;
    }

    /**
     * Checks the runtime class hierarchy (including interfaces) against a set
     * of mapped class names. This avoids loading optional entity classes that
     * may not exist on every runtime.
     */
    private static boolean matches(Class<?> type, String[] classNames) {
        if (type == null) {
            return false;
        }

        String name = type.getName();
        for (String className : classNames) {
            if (name.equals(className)) {
                return true;
            }
        }

        for (Class<?> implemented : type.getInterfaces()) {
            if (matches(implemented, classNames)) {
                return true;
            }
        }

        return matches(type.getSuperclass(), classNames);
    }

    private String[] classNames(String... sourceNames) {
        String[] names = new String[sourceNames.length];
        for (int index = 0; index < sourceNames.length; index++) {
            names[index] = resolver.mapClass(sourceNames[index])
                    .getTargetName().replace('/', '.');
        }
        return names;
    }

    private float partialTicks() {
        Object timer = ReflectionSupport.get(timerField, minecraft);
        if (timer == null) {
            return 0.0F;
        }

        if (partialTicksField == null) {
            partialTicksField = ReflectionSupport.findField(
                    timer.getClass(), partialTicksName);
            if (partialTicksField == null && !partialTicksWarningLogged) {
                partialTicksWarningLogged = true;
                System.err.println("[OrangeX] Cannot read "
                        + partialTicksName + " from " + timer.getClass().getName()
                        + ": world positions will not interpolate");
            }
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

    private Field loadedEntitiesField(Class<?> worldClass) {
        if (loadedEntitiesField == null) {
            loadedEntitiesField = ReflectionSupport.findField(
                    worldClass, loadedEntitiesName);
        }
        return loadedEntitiesField;
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
        private final Method isInvisible;

        private EntityAccess(Class<?> entityClass) {
            MappingClass entity = resolver.mapClass(ENTITY);
            MappingClass living = resolver.mapClass(ENTITY_LIVING);

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
            this.getName = mappedMethod(entity, "getName", "()Ljava/lang/String;",
                    String.class, entityClass);
            this.getHealth = optionalMappedMethod(living, "getHealth", "()F",
                    float.class, entityClass);
            this.getMaxHealth = optionalMappedMethod(living, "getMaxHealth", "()F",
                    float.class, entityClass);
            this.isInvisible = mappedMethod(entity, "isInvisible", "()Z",
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
