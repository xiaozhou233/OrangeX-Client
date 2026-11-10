package orangexbridge;

import cn.xiaozhou233.orangex.event.facade.InputEvents;
import cn.xiaozhou233.orangex.event.facade.Render2DEvents;
import cn.xiaozhou233.orangex.event.facade.TickEvents;

/**
 * Injection bridge.
 *
 * <p>Injected Javassist snippets call this class by its fully-qualified name.
 * The package must not start with {@code cn}: vanilla 1.8.9 has an obfuscated
 * class named {@code cn} in the default package, which makes Javassist treat
 * {@code cn.xiaozhou233...} as a field access instead of a class name.</p>
 *
 * <p>Using a fixed full name also avoids depending on
 * {@code ClassPool#importPackage}, which can be unreliable on launchers with
 * custom class loaders.</p>
 */
public final class Hooks {

    public static void tickPre() {
        TickEvents.firePre();
    }

    public static void tickPost() {
        TickEvents.firePost();
    }

    public static void render2d(Object context, boolean inWorld) {
        Render2DEvents.fire(context, inWorld);
    }

    /**
     * 1.8.9 entry point: the HUD method receives the frame's partial tick, so
     * injected code passes it straight through as {@code $1}.
     */
    public static void render2d(Object context, boolean inWorld,
                                float partialTicks) {
        Render2DEvents.fire(context, inWorld, partialTicks);
    }

    public static void render2d(Object context) {
        Render2DEvents.fire(context);
    }

    public static void inputDraw(Object screen, int mouseX, int mouseY) {
        InputEvents.onDrawScreen(screen, mouseX, mouseY);
    }

    public static boolean inputClick(Object screen, int mouseX,
                                     int mouseY, int button) {
        return InputEvents.onMouseClicked(screen, mouseX, mouseY, button);
    }

    public static boolean inputRelease(Object screen, int mouseX,
                                       int mouseY, int button) {
        return InputEvents.onMouseReleased(screen, mouseX, mouseY, button);
    }

    public static void inputDrag(Object screen, int mouseX,
                                 int mouseY, int button) {
        InputEvents.onMouseDragged(screen, mouseX, mouseY, button);
    }

    public static boolean inputKeyTyped(Object screen, int keyCode) {
        return InputEvents.onKeyTyped(screen, keyCode);
    }

    public static void inputKeyPressed(int keyCode) {
        InputEvents.onKeyPressed(keyCode);
    }

    public static void inputGlfwKeyPressed(int glfwKeyCode) {
        InputEvents.onGlfwKeyPressed(glfwKeyCode);
    }

    public static boolean isOurScreen(Object screen) {
        return InputEvents.isOurInputScreen(screen);
    }

    private Hooks() {
    }
}
