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

import java.lang.reflect.Modifier;

/**
 * Fires {@link cn.xiaozhou233.orangex.event.impl.EventRender2D} once per frame.
 *
 * <ul>
 *   <li>1.8.9: tail of {@code GuiIngame#renderGameOverlay(float)}. This method
 *       is only called while a level is loaded, so the overlay is world-only
 *       by construction.</li>
 *   <li>26.2: tail of {@code Gui#extractRenderState(DeltaTracker, boolean,
 *       boolean)}. The {@code Gui} instance is passed to the facade so the
 *       font backend can reach its {@code GuiRenderState}; the second
 *       argument ({@code shouldRenderLevel}) tells the manager whether a
 *       level is loaded, because this method also runs on the title screen.</li>
 * </ul>
 */
public final class Render2DHook extends ClassHook {

    private static final RuntimeInfo runtimeInfo =
            OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

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

    /**
     * Finds the HUD entry point in a Forge class whose member names depend on
     * the remapping stage (Notch {@code a}, SRG {@code func_175180_a}, MCP
     * {@code renderGameOverlay}).
     */
    private static CtMethod findFloatOverlayMethod(CtClass ctClass) {
        CtMethod fallback = null;
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (!"(F)V".equals(method.getSignature())
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String name = method.getName();
            if ("renderGameOverlay".equals(name)
                    || "func_175180_a".equals(name)
                    || "a".equals(name)) {
                return method;
            }
            if (fallback == null) {
                fallback = method;
            }
        }
        return fallback;
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9: {
                String methodName;
                String methodDescriptor;
                if (runtimeInfo.getLoaderType() == LoaderType.FORGE
                        && classPresent("net.minecraftforge.client.GuiIngameForge")) {
                    // Forge's GuiIngameForge is remapped to SRG names at
                    // runtime (func_175180_a), but the universal jar contains
                    // the Notch name (a). Locate the override by its descriptor
                    // instead of hardcoding any of them.
                    CtMethod method = findFloatOverlayMethod(ctClass);
                    if (method == null) {
                        throw new IllegalStateException(
                                "No (F)V HUD method found in " + ctClass.getName());
                    }
                    method.insertAfter(
                            "orangexbridge.Hooks.render2d(null, true, $1);");
                    return;
                } else {
                    MappingMethod target = resolver.mapMethod(
                            "net/minecraft/client/gui/GuiIngame",
                            "renderGameOverlay",
                            "(F)V");
                    methodName = target.getTargetName();
                    methodDescriptor = target.getTargetDescriptor();
                }
                CtMethod method = ctClass.getMethod(methodName, methodDescriptor);
                // $1 is the frame's partial tick: pass it through so features
                // interpolate world positions exactly like the world render.
                method.insertAfter(
                        "orangexbridge.Hooks.render2d(null, true, $1);");
                break;
            }
            case V26_2: {
                MappingMethod target = resolver.mapMethod(
                        "net/minecraft/client/gui/Gui",
                        "extractRenderState",
                        "(Lnet/minecraft/client/DeltaTracker;ZZ)V");
                CtMethod method = ctClass.getMethod(
                        target.getTargetName(), target.getTargetDescriptor());
                // $0 is this, $1 is the DeltaTracker, $2 is shouldRenderLevel:
                // false on the title screen, true while a level (including its
                // pause/ClickGUI screens) exists.
                method.insertAfter("orangexbridge.Hooks.render2d($0, $2);");
                break;
            }
            default:
                break;
        }
    }
}
