package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.render.api.Render2D;

/**
 * Fired once per frame while the game collects its GUI. Listeners can queue
 * text through {@link #getRender2D()}.
 */
public class EventRender2D {

    private final Render2D render2D;

    public EventRender2D(Render2D render2D) {
        this.render2D = render2D;
    }

    public Render2D getRender2D() {
        return render2D;
    }
}
