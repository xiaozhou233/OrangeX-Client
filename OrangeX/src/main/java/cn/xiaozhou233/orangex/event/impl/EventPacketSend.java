package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.event.Cancellable;
import cn.xiaozhou233.orangex.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.Packet;

@Getter
@AllArgsConstructor
public class EventPacketSend extends Cancellable {
    private Packet packet;
}
