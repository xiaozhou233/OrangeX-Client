package cn.xiaozhou233.orangex.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventMouseButton {
    private final int button;
    private final boolean pressed;
    private final int x;
    private final int y;
}
