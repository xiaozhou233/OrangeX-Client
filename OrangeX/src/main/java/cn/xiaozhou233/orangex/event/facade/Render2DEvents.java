package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.render.Render2DManager;

/**
 * Entry point used by injected code. Always refer to this class by its simple
 * name inside Javassist snippets (see docs/hooks.md).
 */
public final class Render2DEvents {

    private static boolean failureLogged;

    /**
     * @param inWorld whether the game is currently rendering a loaded level.
     *                Modern versions call the GUI extraction on the title
     *                screen too, so the hook must pass this through.
     */
    public static void fire(Object platformContext, boolean inWorld) {
        fire(platformContext, inWorld, Float.NaN);
    }

    /**
     * @param partialTicks the partial tick the game rendered this frame with.
     *                     {@link Float#NaN} when the hook cannot supply it.
     */
    public static void fire(Object platformContext, boolean inWorld,
                            float partialTicks) {
        try {
            Render2DManager manager = OrangeX.getInstance().getRender2DManager();
            if (manager != null) {
                manager.fire(platformContext, inWorld, partialTicks);
            }
        } catch (Throwable throwable) {
            if (!failureLogged) {
                failureLogged = true;
                System.err.println("[OrangeX] Render2D callback failed");
                throwable.printStackTrace();
            }
        }
    }

    /**
     * Legacy entry point for hooks that only run in world (1.8.9 HUD).
     */
    public static void fire(Object platformContext) {
        fire(platformContext, true);
    }

    private Render2DEvents() {
    }
}
