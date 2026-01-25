package cn.xiaozhou233.orangex.module.impl.movement;

import cn.xiaozhou233.orangex.event.impl.EventLoop;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import net.minecraft.entity.EntityLivingBase;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;

public class NoJumpDelay extends Module {
    private static Field jumpTicksField;

    static {
        try {
            jumpTicksField = EntityLivingBase.class.getDeclaredField("jumpTicks");
            jumpTicksField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NoJumpDelay() {
        super("NoJumpDelay", ModuleCategory.MOVEMENT);
    }

    @Subscribe
    public void onEventLoop(EventLoop event) {
        if (mc.thePlayer == null) return;

        try {
            jumpTicksField.setInt(mc.thePlayer, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
