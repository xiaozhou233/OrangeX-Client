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
 * Forwards 1.8.9 {@code GuiScreen} input to the OrangeX empty input screen.
 *
 * <p>1.8.9 has no {@code mouseMoved} callback, so the cursor position is
 * sampled from {@code drawScreen(int, int, float)} every frame. Clicks are
 * taken from the screen's normal mouse pipeline.</p>
 *
 * <p>26.2 does not need this hook: its input screen overrides the
 * {@code Screen} callbacks directly.</p>
 */
public final class GuiInputHook extends ClassHook {

    private static final String GUI_SCREEN = "net/minecraft/client/gui/GuiScreen";

    private static final RuntimeInfo runtimeInfo =
            OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return resolver.mapClass(GUI_SCREEN)
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

        injectBefore(ctClass, "drawScreen", "(IIF)V",
                "orangexbridge.Hooks.inputDraw($0, $1, $2);");

        injectBefore(ctClass, "mouseClicked", "(III)V",
                "if (orangexbridge.Hooks.inputClick($0, $1, $2, $3)) return;");

        injectBefore(ctClass, "mouseReleased", "(III)V",
                "if (orangexbridge.Hooks.inputRelease($0, $1, $2, $3)) return;");

        injectBefore(ctClass, "mouseClickMove", "(IIIJ)V",
                "orangexbridge.Hooks.inputDrag($0, $1, $2, $3);");

        // Forward every key typed on our empty screen as an EventKey. The
        // facade only consumes the toggle key so ESC and other keys keep
        // their vanilla Screen behaviour.
        injectBefore(ctClass, "keyTyped", "(CI)V",
                "if (!orangexbridge.Legacy.isRepeatEvent()"
                        + " && orangexbridge.Hooks.inputKeyTyped($0, $2)) return;");

        // The empty screen is an overlay: it must not pause singleplayer.
        injectAfter(ctClass, "doesGuiPauseGame", "()Z",
                "if (orangexbridge.Hooks.isOurScreen($0)) { $_ = false; }");

        // Wheel scrolling for the ClickGUI. Injecting after vanilla keeps its
        // mouse-button dispatch intact; the wheel event is still readable.
        try {
            injectAfter(ctClass, "handleMouseInput", "()V",
                    "orangexbridge.Legacy.forwardScroll($0);");
        } catch (Throwable ignored) {
            // Scrolling is optional: never let it disable the input hooks
            // above on a runtime where this method cannot be resolved.
        }
    }

    private void injectBefore(CtClass ctClass, String name,
                              String descriptor, String code) throws Exception {
        CtMethod method = find(ctClass, name, descriptor);
        method.insertBefore(code);
    }

    private void injectAfter(CtClass ctClass, String name,
                             String descriptor, String code) throws Exception {
        CtMethod method = find(ctClass, name, descriptor);
        method.insertAfter(code);
    }

    private CtMethod find(CtClass ctClass, String name, String descriptor)
            throws Exception {
        MappingMethod mapping = resolver.mapMethod(GUI_SCREEN, name, descriptor);
        return ctClass.getMethod(
                mapping.getTargetName(), mapping.getTargetDescriptor());
    }
}
