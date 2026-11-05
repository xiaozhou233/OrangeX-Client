package cn.xiaozhou233.orangex.hook.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.hook.ClassHook;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Fires {@link cn.xiaozhou233.orangex.event.impl.EventRender2D} once per frame.
 *
 * <ul>
 *   <li>1.8.9: tail of {@code GuiIngame#renderGameOverlay(float)}.</li>
 *   <li>26.2: tail of {@code Gui#extractRenderState(DeltaTracker, boolean,
 *       boolean)}. The {@code Gui} instance is passed to the facade so the
 *       font backend can reach its {@code GuiRenderState}.</li>
 * </ul>
 */
public final class Render2DHook extends ClassHook {

    private static final RuntimeInfo runtimeInfo =
            OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    @Override
    protected String[] getInjectedClassNames() {
        return new String[]{
                "cn.xiaozhou233.orangex.event.facade.Render2DEvents"
        };
    }

    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                if (runtimeInfo.getLoaderType() == LoaderType.FORGE
                        && classPresent("net.minecraftforge.client.GuiIngameForge")) {
                    // Forge replaces the HUD renderer with its own subclass.
                    return "net.minecraftforge.client.GuiIngameForge";
                }
                return resolver.mapClass("net/minecraft/client/gui/GuiIngame")
                        .getTargetName().replace("/", ".");
            case V26_2:
                return "net.minecraft.client.gui.Gui";
            default:
                return null;
        }
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, Render2DHook.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9: {
                String methodName;
                String methodDescriptor;
                if (runtimeInfo.getLoaderType() == LoaderType.FORGE
                        && classPresent("net.minecraftforge.client.GuiIngameForge")) {
                    // Forge's GuiIngameForge is not obfuscated.
                    methodName = "renderGameOverlay";
                    methodDescriptor = "(F)V";
                } else {
                    MappingMethod target = resolver.mapMethod(
                            "net/minecraft/client/gui/GuiIngame",
                            "renderGameOverlay",
                            "(F)V");
                    methodName = target.getTargetName();
                    methodDescriptor = target.getTargetDescriptor();
                }
                CtMethod method = ctClass.getMethod(methodName, methodDescriptor);
                method.insertAfter("Render2DEvents.fire(null);");
                System.out.println("[OrangeX] Render2DHook injected: "
                        + ctClass.getName() + "#" + methodName + methodDescriptor);
                break;
            }
            case V26_2: {
                MappingMethod target = resolver.mapMethod(
                        "net/minecraft/client/gui/Gui",
                        "extractRenderState",
                        "(Lnet/minecraft/client/DeltaTracker;ZZ)V");
                CtMethod method = ctClass.getMethod(
                        target.getTargetName(), target.getTargetDescriptor());
                method.insertAfter("Render2DEvents.fire($0);");
                System.out.println("[OrangeX] Render2DHook injected: "
                        + ctClass.getName() + "#" + target.getTargetName()
                        + target.getTargetDescriptor());
                break;
            }
            default:
                break;
        }
    }
}
