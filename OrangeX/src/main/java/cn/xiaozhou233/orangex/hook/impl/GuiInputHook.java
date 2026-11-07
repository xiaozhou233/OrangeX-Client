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
    protected String[] getInjectedClassNames() {
        return new String[]{
                "cn.xiaozhou233.orangex.event.facade.InputEvents",
                "cn.xiaozhou233.orangex.input.internal.v1_8_9.V1_8_9Input"
        };
    }

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
                "InputEvents.onDrawScreen($0, $1, $2);");

        injectBefore(ctClass, "mouseClicked", "(III)V",
                "if (InputEvents.onMouseClicked($0, $1, $2, $3)) return;");

        injectBefore(ctClass, "mouseReleased", "(III)V",
                "if (InputEvents.onMouseReleased($0, $1, $2, $3)) return;");

        injectBefore(ctClass, "mouseClickMove", "(IIIJ)V",
                "InputEvents.onMouseDragged($0, $1, $2, $3);");

        // Close with M while the empty screen is open. Repeat events are
        // ignored so holding the key does not bounce the screen.
        injectBefore(ctClass, "keyTyped", "(CI)V",
                "if ($2 == V1_8_9Input.TOGGLE_KEY"
                        + " && !V1_8_9Input.isRepeatEvent()"
                        + " && InputEvents.onKeyTyped($0, $2)) return;");

        // The empty screen is an overlay: it must not pause singleplayer.
        injectAfter(ctClass, "doesGuiPauseGame", "()Z",
                "if (InputEvents.isOurInputScreen($0)) { $_ = false; }");
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
