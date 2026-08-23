package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventKey extends Event {
    private final int key;
}