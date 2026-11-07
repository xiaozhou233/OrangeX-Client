package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A key press delivered through the EventBus.
 *
 * <p>{@link #getKey()} always uses the 1.8.9 / LWJGL 2 numbering, even when
 * the game runs on 26.2. Modern GLFW key codes are translated before the
 * event is posted (see {@code KeyCodes#fromGlfw(int)}).</p>
 */
@Getter
@AllArgsConstructor
public class EventKey implements Event {
    private final int key;
}
