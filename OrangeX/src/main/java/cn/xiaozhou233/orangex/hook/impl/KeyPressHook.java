package cn.xiaozhou233.orangex.hook.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.hook.ClassHook;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Key-press hook for both supported versions.
 *
 * <ul>
 *   <li>1.8.9: {@code KeyBinding#onTick(int)} is called for every key press
 *       while no screen is open. This keeps short taps from being missed
 *       between two 20 Hz client ticks.</li>
 *   <li>26.2: {@code KeyboardHandler#keyPress(long, int, KeyEvent)} is the
 *       GLFW callback and therefore runs for every key press, including while
 *       walking with no screen open. The input screen's own
 *       {@code Screen#keyPressed} is not enough because the game does not
 *       deliver keys to a screen that is not open.</li>
 * </ul>
 */
public final class KeyPressHook extends ClassHook {

    private static final String KEY_BINDING =
            "net/minecraft/client/settings/KeyBinding";
    private static final String KEYBOARD_HANDLER =
            "net/minecraft/client/KeyboardHandler";

    /** GLFW_PRESS. */
    private static final int GLFW_PRESS = 1;

    private static final RuntimeInfo runtimeInfo =
            OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return resolver.mapClass(KEY_BINDING)
                        .getTargetName().replace("/", ".");
            case V26_2:
                return resolver.mapClass(KEYBOARD_HANDLER)
                        .getTargetName().replace("/", ".");
            default:
                return null;
        }
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                transformLegacy(ctClass);
                break;
            case V26_2:
                transformModern(ctClass);
                break;
            default:
                break;
        }
    }

    private void transformLegacy(CtClass ctClass) throws Exception {
        MappingMethod mapping = resolver.mapMethod(
                KEY_BINDING, "onTick", "(I)V");
        CtMethod method = ctClass.getMethod(
                mapping.getTargetName(), mapping.getTargetDescriptor());
        // $1 is the key code. Ignore auto-repeat so holding a key does not
        // flood the EventBus. Every other key is forwarded as an EventKey.
        method.insertBefore(
                "if (!orangexbridge.Legacy.isRepeatEvent())"
                        + " { orangexbridge.Hooks.inputKeyPressed($1); }");
    }

    private void transformModern(CtClass ctClass) throws Exception {
        MappingMethod mapping = resolver.mapMethod(
                KEYBOARD_HANDLER,
                "keyPress",
                "(JILnet/minecraft/client/input/KeyEvent;)V");
        CtMethod method = ctClass.getMethod(
                mapping.getTargetName(), mapping.getTargetDescriptor());
        // $2 is the GLFW action (1 = press, 0 = release, 2 = repeat).
        // $3 is the KeyEvent record; its key() is a GLFW key code, which the
        // facade normalises to the 1.8.9 numbering.
        method.insertBefore(
                "if ($2 == " + GLFW_PRESS + ")"
                        + " { orangexbridge.Hooks.inputGlfwKeyPressed($3.key()); }");
    }
}
