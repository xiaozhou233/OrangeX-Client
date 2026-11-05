package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.render.Render2DManager;

/**
 * Entry point used by injected code. Always refer to this class by its simple
 * name inside Javassist snippets (see docs/hooks.md).
 */
public final class Render2DEvents {

    private static boolean failureLogged;

    public static void fire(Object platformContext) {
        try {
            Render2DManager manager = OrangeX.getInstance().getRender2DManager();
            if (manager != null) {
                manager.fire(platformContext);
            }
        } catch (Throwable throwable) {
            if (!failureLogged) {
                failureLogged = true;
                System.err.println("[OrangeX] Render2D callback failed");
                throwable.printStackTrace();
            }
        }
    }

    private Render2DEvents() {
    }
}
