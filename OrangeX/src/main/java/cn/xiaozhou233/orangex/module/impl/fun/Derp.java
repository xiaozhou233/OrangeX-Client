package cn.xiaozhou233.orangex.module.impl.fun;

import cn.xiaozhou233.orangex.event.impl.EventLoop;
import cn.xiaozhou233.orangex.event.impl.EventPacketSend;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Derp extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private float yaw = 0f;

    private final NumberValue speed =
            new NumberValue("Speed", "Speed", 10d, 1d, 100d, 0.1d);
    private final BooleanValue serverOnly =
            new BooleanValue("ServerOnly", "ServerOnly", false);
    private final ModeValue mode =
            new ModeValue("Mode", "Mode", "Derp", Arrays.asList("Derp", "Random", "Jitter"));

    public Derp() {
        super("Derp", ModuleCategory.FUN);

        addValue(speed);
        addValue(serverOnly);
        addValue(mode);

        setEnabled(false);
    }

    @Override
    public void onEnable() {
        yaw = mc.thePlayer != null ? mc.thePlayer.rotationYaw : 0f;
    }

    @Subscribe
    public void onLoop(EventLoop event) {
        float spd = speed.getValue().floatValue();

        switch (mode.getValue()) {
            case "Derp":
                yaw += spd;
                break;

            case "Random":
                yaw = (float) (Math.random() * 360f - 180f);
                break;

            case "Jitter":
                yaw += MathHelper.clamp_float(
                        (float) ((Math.random() * 2f - 1f) * spd),
                        -15f,
                        15f
                );
                break;
        }

        yaw = MathHelper.wrapAngleTo180_float(yaw);
    }


    @Subscribe
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof C03PacketPlayer))
            return;
        if (mc.thePlayer == null)
            return;

        C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
        setYawPitch(packet, yaw, mc.thePlayer.rotationPitch);

        if (!serverOnly.getValue()) {
            mc.thePlayer.renderYawOffset = yaw;
            mc.thePlayer.rotationYawHead = yaw;
        }
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
