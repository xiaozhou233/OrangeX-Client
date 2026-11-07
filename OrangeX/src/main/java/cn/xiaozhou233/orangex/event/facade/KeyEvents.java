package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.input.KeyCodes;
import org.greenrobot.eventbus.EventBus;

/**
 * Entry point for raw key events.
 *
 * <p>Injected code and version-specific input code only call this facade;
 * subscribers receive {@link EventKey} through the shared EventBus. All key
 * codes are normalised to the 1.8.9 / LWJGL 2 numbering.</p>
 */
public final class KeyEvents {

    private static final EventBus BUS = OrangeX.getInstance().getEventBus();

    private static boolean failureLogged;

    /**
     * @param key canonical 1.8.9 (LWJGL 2) key code.
     */
    public static void fire(int key) {
        if (key == KeyCodes.NONE) {
            return;
        }
        try {
            BUS.post(new EventKey(key));
        } catch (Throwable throwable) {
            if (!failureLogged) {
                failureLogged = true;
                System.err.println("[OrangeX] Key event dispatch failed");
                throwable.printStackTrace();
            }
        }
    }

    private KeyEvents() {
    }
}
