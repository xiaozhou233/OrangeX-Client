package cn.xiaozhou233.orangex.wrapper.internal.v26_2;

import cn.xiaozhou233.orangex.input.internal.VersionInput;
import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Minecraft 26.2 implementation of the Minecraft wrapper.
 *
 * <p>26.x runtimes already use Mojang names, so the mapping resolver is an
 * identity mapping. The singleton field is still private and must be read via
 * {@link Class#getDeclaredField(String)}.</p>
 */
public final class V26_2Minecraft implements Minecraft {

    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String INSTANCE_FIELD = "instance";

    private final RuntimeInfo runtimeInfo;
    private final MappingClass mappingMinecraft;

    private Object instance;
    private VersionInput input;

    public V26_2Minecraft(RuntimeInfo runtimeInfo,
                          MappingResolver mappingResolver) {
        this.runtimeInfo = runtimeInfo;
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
     * Loads the version-specific input implementation on first use. Keeping
     * the 26.2 class behind a string keeps 1.8.9 runtimes clean.
     */
    private synchronized VersionInput input() {
        if (input == null) {
            try {
                Class<?> bridge = Class.forName(
                        "cn.xiaozhou233.orangex.input.v26_2.V26_2Input",
                        true,
                        V26_2Minecraft.class.getClassLoader());
                input = (VersionInput) bridge.getConstructor(
                                RuntimeInfo.class,
                                Object.class)
                        .newInstance(runtimeInfo, getMinecraft());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Cannot create the 26.2 input bridge", e);
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
                    "No mapping for field '" + INSTANCE_FIELD + "' on "
                            + MINECRAFT_CLASS);
        }

        // Mapping names are internal names (a/b/C); Class.forName needs
        // binary names (a.b.C).
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
     * The singleton field is private in modern clients. Search the declaring
     * class first and walk superclasses in case it is inherited.
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
