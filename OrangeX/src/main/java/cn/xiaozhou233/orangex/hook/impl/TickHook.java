package cn.xiaozhou233.orangex.hook.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.hook.ClassHook;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Fires {@code EventTickPre}/{@code EventTickPost} around
 * {@code Minecraft#runTick}. The descriptor differs per version.
 */
public final class TickHook extends ClassHook {

    private static final RuntimeInfo runtimeInfo = OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    @Override
    protected String[] getInjectedClassNames() {
        return new String[]{"cn.xiaozhou233.orangex.event.facade.TickEvents"};
    }

    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return resolver.mapClass("net/minecraft/client/Minecraft").getTargetName().replace("/", ".");
            case V26_2:
                return "net.minecraft.client.Minecraft";
            default:
                return null;
        }
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        String methodName = null;
        String methodDesc = null;

        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9: {
                MappingMethod runTick = resolver.mapMethod(
                        "net/minecraft/client/Minecraft", "runTick", "()V");
                methodName = runTick.getTargetName();
                methodDesc = runTick.getTargetDescriptor();
                break;
            }
            // 26.2 is not obfuscated: runTick(Z)V, private.
            case V26_2: {
                MappingMethod runTick = resolver.mapMethod(
                        "net/minecraft/client/Minecraft", "runTick", "(Z)V");
                methodName = runTick.getTargetName();
                methodDesc = runTick.getTargetDescriptor();
                break;
            }
            default:
                break;
        }

        if (methodName == null || methodDesc == null) {
            return;
        }

        CtMethod method = ctClass.getMethod(methodName, methodDesc);

        // Simple names: the package is imported by ClassPoolUtils.
        // Fully qualified "cn.*" names collide with the vanilla "cn" class.
        method.insertBefore("TickEvents.firePre();");
        method.insertAfter("TickEvents.firePost();");
    }
}
