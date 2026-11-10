package cn.xiaozhou233.orangex.input.internal.v1_8_9;

import cn.xiaozhou233.orangex.event.facade.InputEvents;
import cn.xiaozhou233.orangex.hook.ClassPoolUtils;
import cn.xiaozhou233.orangex.hook.HookManager;
import cn.xiaozhou233.orangex.hook.JuiceAgentBridge;
import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.input.internal.VersionInput;
import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 1.8.9 input screen.
 *
 * <p>{@code GuiScreen} is abstract in 1.8.9, so a concrete empty subclass is
 * generated at runtime and defined into the game class loader. Opening it via
 * {@code Minecraft#displayGuiScreen} calls {@code setIngameNotInFocus()},
 * which ungrabs the cursor, and while {@code currentScreen != null} the
 * player's mouse and keyboard input is not processed.</p>
 *
 * <p>This class imports LWJGL 2 directly and is therefore loaded only on
 * 1.8.9 through a reflective factory in the wrapper.</p>
 */
public final class V1_8_9Input implements VersionInput {

    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String GUI_SCREEN_CLASS = "net/minecraft/client/gui/GuiScreen";
    private static final String GENERATED_SCREEN =
            "cn.xiaozhou233.orangex.input.internal.v1_8_9.GeneratedInputScreen";

    private final Object minecraft;
    private final ClassLoader loader;

    private final Class<?> guiScreenClass;
    private final Field currentScreenField;
    private final Field worldField;
    private final Field screenWidthField;
    private final Field screenHeightField;
    private final Method displayGuiScreen;

    private Object shellScreen;
    private Class<?> inputScreenClass;

    public V1_8_9Input(RuntimeInfo runtimeInfo,
                       MappingResolver resolver,
                       Object minecraft) {
        this.minecraft = minecraft;

        MappingClass minecraftMapping = resolver.mapClass(MINECRAFT_CLASS);
        MappingClass guiScreenMapping = resolver.mapClass(GUI_SCREEN_CLASS);

        ClassLoader loader = runtimeInfo.getMinecraftClassLoader();
        this.loader = loader;
        Class<?> minecraftClass = loadClass(
                minecraftMapping.getTargetName(), loader);

        this.guiScreenClass = loadClass(
                guiScreenMapping.getTargetName(), loader);
        this.currentScreenField = findField(
                minecraftClass,
                minecraftMapping.getField("currentScreen").getTargetName());
        this.worldField = findField(
                minecraftClass,
                minecraftMapping.getField("theWorld").getTargetName());

        this.screenWidthField = findField(
                guiScreenClass,
                guiScreenMapping.getField("width").getTargetName());
        this.screenHeightField = findField(
                guiScreenClass,
                guiScreenMapping.getField("height").getTargetName());

        MappingMethod displayMapping = resolver.mapMethod(
                MINECRAFT_CLASS,
                "displayGuiScreen",
                "(Lnet/minecraft/client/gui/GuiScreen;)V");
        this.displayGuiScreen = findDisplayGuiScreen(
                minecraftClass, displayMapping.getTargetName());
    }

    private static Class<?> loadClass(String internalName, ClassLoader loader) {
        try {
            return Class.forName(
                    internalName.replace('/', '.'), false, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot load " + internalName, e);
        }
    }

    private static Field findField(Class<?> owner, String name) {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Continue with the superclass.
            }
        }
        throw new IllegalStateException(
                "Cannot find field '" + name + "' on " + owner.getName());
    }

    private Method findDisplayGuiScreen(Class<?> minecraftClass, String name) {
        for (Method method : minecraftClass.getDeclaredMethods()) {
            if (!method.getName().equals(name)
                    || method.getParameterTypes().length != 1
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterTypes()[0].isAssignableFrom(guiScreenClass)) {
                method.setAccessible(true);
                return method;
            }
        }
        try {
            Method method = minecraftClass.getDeclaredMethod(name, guiScreenClass);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Cannot find displayGuiScreen on " + minecraftClass.getName(), e);
        }
    }

    @Override
    public boolean isInWorld() {
        try {
            return worldField.get(minecraft) != null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the world field", e);
        }
    }

    @Override
    public boolean isInputScreenOpen() {
        if (shellScreen == null) {
            return false;
        }
        try {
            return currentScreenField.get(minecraft) == shellScreen;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the current screen", e);
        }
    }

    @Override
    public boolean isScreenOpen() {
        try {
            return currentScreenField.get(minecraft) != null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the current screen", e);
        }
    }

    @Override
    public boolean isInputScreenInstance(Object screen) {
        return shellScreen != null && shellScreen == screen;
    }

    @Override
    public void openInputScreen() {
        if (isInputScreenOpen()) {
            return;
        }
        try {
            Object screen = inputScreenClass().getDeclaredConstructor().newInstance();
            shellScreen = screen;
            displayGuiScreen.invoke(minecraft, screen);
            ClientInput.onScreenOpened(getGuiMouseX(), getGuiMouseY());
        } catch (ReflectiveOperationException e) {
            shellScreen = null;
            throw new IllegalStateException("Cannot open the 1.8.9 input screen", e);
        }
    }

    /**
     * {@code GuiScreen} is abstract in 1.8.9, so generate a concrete empty
     * subclass at runtime and define it into the game class loader. Doing it
     * with bytecode avoids hardcoding obfuscated class names.
     */
    private synchronized Class<?> inputScreenClass() {
        if (inputScreenClass != null) {
            return inputScreenClass;
        }

        try {
            ClassPool pool = ClassPoolUtils.createPool(loader);

            CtClass base;
            byte[] cachedBase = HookManager.getTargetBytes(
                    guiScreenClass.getName());
            if (cachedBase != null) {
                // Prefer the original bytes captured before our hooks were
                // applied: retransforming GuiScreen again could drop the
                // injected methods.
                base = pool.makeClass(new ByteArrayInputStream(cachedBase));
            } else {
                try {
                    base = pool.get(guiScreenClass.getName());
                } catch (Throwable notFound) {
                    // Some launch environments hide transformed class resources;
                    // fall back to the bytes the agent can read for the loaded class.
                    byte[] baseBytes = JuiceAgentBridge.getClassBytesByName(
                            guiScreenClass.getName().replace('.', '/'));
                    if (baseBytes == null) {
                        throw new IllegalStateException(
                                "Cannot load " + guiScreenClass.getName(), notFound);
                    }
                    base = pool.makeClass(new ByteArrayInputStream(baseBytes));
                }
            }

            CtClass generated = pool.makeClass(GENERATED_SCREEN);
            generated.setModifiers(Modifier.PUBLIC);
            generated.setSuperclass(base);

            CtConstructor constructor = new CtConstructor(new CtClass[0], generated);
            constructor.setModifiers(Modifier.PUBLIC);
            constructor.setBody("super();");
            generated.addConstructor(constructor);

            byte[] bytes;
            try {
                bytes = generated.toBytecode();
            } finally {
                generated.detach();
                base.detach();
            }

            Class<?> defined = JuiceAgentBridge.defineClass(loader, bytes);
            if (defined == null) {
                throw new IllegalStateException(
                        "JuiceAgent.defineClass returned null");
            }
            inputScreenClass = defined;
            return inputScreenClass;
        } catch (Throwable throwable) {
            throw new IllegalStateException(
                    "Cannot generate the 1.8.9 input screen", throwable);
        }
    }

    @Override
    public void closeInputScreen() {
        if (!isInputScreenOpen()) {
            shellScreen = null;
            ClientInput.reset();
            return;
        }
        try {
            displayGuiScreen.invoke(minecraft, new Object[]{null});
            shellScreen = null;
            ClientInput.reset();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot close the 1.8.9 input screen", e);
        }
    }

    /**
     * Used by injected {@code GuiScreen} code to ignore LWJGL auto-repeat
     * events.
     */
    public static boolean isRepeatEvent() {
        return Keyboard.isRepeatEvent();
    }

    /**
     * Called after {@code GuiScreen#handleMouseInput} ran. The wheel event is
     * still readable at this point and must not consume the vanilla mouse
     * button dispatch.
     */
    public static void forwardScroll(Object screen) {
        if (!InputEvents.isOurInputScreen(screen)) {
            return;
        }

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            ClientInput.onMouseScroll(wheel > 0 ? 1.0 : -1.0);
        }
    }

    @Override
    public double getGuiMouseX() {
        Object screen = shellScreen;
        if (screen == null) {
            return 0.0;
        }
        try {
            int width = screenWidthField.getInt(screen);
            return (double) Mouse.getX() * (double) width / (double) Display.getWidth();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the GUI width", e);
        }
    }

    @Override
    public double getGuiMouseY() {
        Object screen = shellScreen;
        if (screen == null) {
            return 0.0;
        }
        try {
            int height = screenHeightField.getInt(screen);
            return (double) height
                    - (double) Mouse.getY() * (double) height / (double) Display.getHeight()
                    - 1.0;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the GUI height", e);
        }
    }
}
