package orangexbridge;

import cn.xiaozhou233.orangex.input.internal.v1_8_9.V1_8_9Input;

/**
 * 1.8.9-only injection bridge.
 *
 * <p>Kept separate from {@link Hooks} so a 26.2 runtime never has to resolve
 * the LWJGL 2 classes used by {@link V1_8_9Input}.</p>
 */
public final class Legacy {

    public static boolean isRepeatEvent() {
        return V1_8_9Input.isRepeatEvent();
    }

    private Legacy() {
    }
}
