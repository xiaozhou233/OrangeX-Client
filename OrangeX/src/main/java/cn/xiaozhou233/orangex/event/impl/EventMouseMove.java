package cn.xiaozhou233.orangex.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventMouseMove {
    private final int x;
    private final int y;
}
