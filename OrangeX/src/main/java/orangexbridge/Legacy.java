package orangexbridge;

import cn.xiaozhou233.orangex.input.internal.v1_8_9.V1_8_9Input;

/**
 * 1.8.9-only injection bridge.
 *
 * <p>Kept separate from {@link Hooks} so a 26.2 runtime never has to resolve
 * the LWJGL 2 classes used by {@link V1_8_9Input}.</p>
 */
public final class Legacy {

    /** LWJGL 2 {@code Keyboard.KEY_M}. */
    public static final int TOGGLE_KEY = 50;

    public static boolean isRepeatEvent() {
        return V1_8_9Input.isRepeatEvent();
    }

    private Legacy() {
    }
}
