package cn.xiaozhou233.orangex.module.impl.fun;

import cn.xiaozhou233.orangex.event.impl.EventLoop;
import cn.xiaozhou233.orangex.event.impl.EventPacketSend;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;

public class Derp extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private float yaw = 0f;

    public Derp() {
        super("Derp", ModuleCategory.FUN);

        setEnabled(false);
    }

    @Subscribe
    public void onLoop(EventLoop event) {
        yaw += 20f;
        if (yaw > 180f) yaw -= 360f;
    }

    @Subscribe
    public void onPacketSend(EventPacketSend event) {
        // C03PacketPlayer Only
        if (!(event.getPacket() instanceof C03PacketPlayer))
            return;
        if (mc.thePlayer == null)
            return;

        C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
        setYawPitch(packet, yaw, mc.thePlayer.rotationPitch);

        mc.thePlayer.renderYawOffset = yaw;
        mc.thePlayer.rotationYawHead = yaw;
    }

    private void setYawPitch(C03PacketPlayer packet, float yaw, float pitch) {
        try {
            Field yawField = C03PacketPlayer.class.getDeclaredField("yaw");
            Field pitchField = C03PacketPlayer.class.getDeclaredField("pitch");

            yawField.setAccessible(true);
            pitchField.setAccessible(true);

            yawField.setFloat(packet, yaw);
            pitchField.setFloat(packet, pitch);
        } catch (Exception ignored) {
        }
    }

}
