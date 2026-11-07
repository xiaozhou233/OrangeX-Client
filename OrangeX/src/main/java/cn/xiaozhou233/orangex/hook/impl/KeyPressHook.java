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
 * 1.8.9 key-press hook.
 *
 * <p>{@code KeyBinding#onTick(int)} is called for every key press while no
 * screen is open. Using it for the GUI toggle avoids missing a short tap
 * between two 20 Hz client ticks.</p>
 */
public final class KeyPressHook extends ClassHook {

    private static final String KEY_BINDING =
            "net/minecraft/client/settings/KeyBinding";

    private static final RuntimeInfo runtimeInfo =
            OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return resolver.mapClass(KEY_BINDING)
                        .getTargetName().replace("/", ".");
            default:
                return null;
        }
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        if (runtimeInfo.getMinecraftVersion() != MinecraftVersion.V1_8_9) {
            return;
        }

        MappingMethod mapping = resolver.mapMethod(
                KEY_BINDING, "onTick", "(I)V");
        CtMethod method = ctClass.getMethod(
                mapping.getTargetName(), mapping.getTargetDescriptor());
        // Static method: $1 is the key code. Ignore auto-repeat so holding
        // M does not toggle repeatedly.
        method.insertBefore(
                "if ($1 == orangexbridge.Legacy.TOGGLE_KEY"
                        + " && !orangexbridge.Legacy.isRepeatEvent())"
                        + " { orangexbridge.Hooks.inputKeyPressed($1); }");
    }
}
