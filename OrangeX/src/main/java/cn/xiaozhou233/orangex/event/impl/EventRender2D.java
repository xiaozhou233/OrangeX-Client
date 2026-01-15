package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.event.Event;
import lombok.Getter;

@Getter
public class EventRender2D extends Event {
    private float partialTicks;

    public EventRender2D(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
