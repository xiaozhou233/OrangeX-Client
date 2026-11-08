package cn.xiaozhou233.orangex.ui.clickgui;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Smooths hover and toggle values for ClickGUI elements.
 *
 * <p>Keys are compared by identity so equal-but-distinct modules or values do
 * not share an animation.</p>
 */
public final class ClickGuiAnimator {

    private static final float HOVER_SPEED = 14.0f;
    private static final float STATE_SPEED = 16.0f;

    private final Map<Object, Float> hoverAnimations =
            new IdentityHashMap<Object, Float>();
    private final Map<Object, Float> stateAnimations =
            new IdentityHashMap<Object, Float>();

    public float hover(Object key, boolean hovered, float delta) {
        if (!hoverAnimations.containsKey(key)) {
            hoverAnimations.put(key, 0.0f);
        }
        return animate(hoverAnimations, key, hovered ? 1.0f : 0.0f,
                delta, HOVER_SPEED);
    }

    public float state(Object key, boolean state, float delta) {
        return animate(stateAnimations, key, state ? 1.0f : 0.0f,
                delta, STATE_SPEED);
    }

    private static float animate(Map<Object, Float> animations, Object key,
                                 float target, float delta, float speed) {
        Float stored = animations.get(key);
        float current = stored == null ? target : stored.floatValue();

        if (Math.abs(target - current) < 0.001f) {
            animations.put(key, target);
            return target;
        }

        float factor = 1.0f - (float) Math.exp(-speed * delta);
        float next = current + (target - current) * factor;
        if (Math.abs(target - next) < 0.002f) {
            next = target;
        }
        animations.put(key, next);
        return next;
    }
}
