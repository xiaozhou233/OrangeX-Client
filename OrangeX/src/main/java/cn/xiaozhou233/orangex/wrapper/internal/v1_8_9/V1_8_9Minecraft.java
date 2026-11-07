package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

import cn.xiaozhou233.orangex.input.internal.VersionInput;
import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 1.8.9 implementation of the Minecraft wrapper.
 *
 * <p>All Minecraft class and member names are resolved through
 * {@link MappingResolver}; the literal names used here are MCP (named) names,
 * never obfuscated or SRG names.</p>
 */
public final class V1_8_9Minecraft implements Minecraft {

    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String INSTANCE_FIELD = "theMinecraft";

    private final RuntimeInfo runtimeInfo;
    private final MappingResolver mappingResolver;
    private final MappingClass mappingMinecraft;

    private Object instance;
    private VersionInput input;

    public V1_8_9Minecraft(RuntimeInfo runtimeInfo, MappingResolver mappingResolver) {
        this.runtimeInfo = runtimeInfo;
        this.mappingResolver = mappingResolver;
        this.mappingMinecraft = mappingResolver.mapClass(MINECRAFT_CLASS);
    }

    @Override
    public synchronized Object getMinecraft() {
        if (instance == null) {
            instance = resolveInstance();
        }
        return instance;
    }

    /**
     * Loads the version-specific input implementation on first use. The class
     * name is kept as a string so a 26.2 runtime never resolves LWJGL 2.
     */
    private synchronized VersionInput input() {
        if (input == null) {
            try {
                Class<?> bridge = Class.forName(
                        "cn.xiaozhou233.orangex.input.internal.v1_8_9.V1_8_9Input",
                        true,
                        V1_8_9Minecraft.class.getClassLoader());
                input = (VersionInput) bridge.getConstructor(
                                RuntimeInfo.class,
                                MappingResolver.class,
                                Object.class)
                        .newInstance(runtimeInfo, mappingResolver, getMinecraft());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Cannot create the 1.8.9 input bridge", e);
            }
        }
        return input;
    }

    @Override
    public boolean isInWorld() {
        return input().isInWorld();
    }

    @Override
    public boolean isInputScreenOpen() {
        return input().isInputScreenOpen();
    }

    @Override
    public boolean isScreenOpen() {
        return input().isScreenOpen();
    }

    @Override
    public boolean isInputScreenInstance(Object screen) {
        return input().isInputScreenInstance(screen);
    }

    @Override
    public void openInputScreen() {
        input().openInputScreen();
    }

    @Override
    public void closeInputScreen() {
        input().closeInputScreen();
    }

    @Override
    public boolean isToggleKeyDown() {
        return input().isToggleKeyDown();
    }

    @Override
    public boolean isToggleEventDriven() {
        return input().isToggleEventDriven();
    }

    @Override
    public double getGuiMouseX() {
        return input().getGuiMouseX();
    }

    @Override
    public double getGuiMouseY() {
        return input().getGuiMouseY();
    }

    private Object resolveInstance() {
        MappingField mappingField = mappingMinecraft.getField(INSTANCE_FIELD);
        if (mappingField == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + INSTANCE_FIELD + "' on " + MINECRAFT_CLASS);
        }

        // Mapping files use internal names (a/b/C); Class.forName needs binary
        // names (a.b.C). Vanilla target names such as "ave" are unaffected.
        String targetClassName = mappingMinecraft.getTargetName().replace('/', '.');
        Class<?> minecraftClass;
        try {
            minecraftClass = Class.forName(
                    targetClassName, false, runtimeInfo.getMinecraftClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot load Minecraft class " + targetClassName, e);
        }

        Field field = findStaticField(minecraftClass, mappingField.getTargetName());
        try {
            field.setAccessible(true);
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read Minecraft instance field " + field.getName(), e);
        }
    }

    /**
     * The singleton field is private in the vanilla client, so {@link
     * Class#getField(String)} cannot see it. Look it up by declaration instead
     * and walk superclasses in case the field is inherited.
     */
    private static Field findStaticField(Class<?> owner, String name) {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                if (Modifier.isStatic(field.getModifiers())) {
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
                // Continue with the superclass.
            }
        }

        throw new IllegalStateException(
                "Cannot find static field '" + name + "' on " + owner.getName());
    }
}
