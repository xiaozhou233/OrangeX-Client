package cn.xiaozhou233.orangex.mixin.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventPacketSend;
import cn.yapeteam.ymixin.annotations.Inject;
import cn.yapeteam.ymixin.annotations.Local;
import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.annotations.Target;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(
            method = "addToSendQueue",
            desc = "(Lnet/minecraft/network/Packet;)V",
            target =  @Target("HEAD")
    )
    public void onSendPacket(@Local(source = "packet", index = 1) Packet packet) {
        EventPacketSend eventPacketSend = new EventPacketSend(packet);
        OrangeX.getInstance().getEventBus().post(eventPacketSend);

        if (eventPacketSend.isCancelled()) {
            return;
        }
    }
}
