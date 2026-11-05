package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventTickPost;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import org.greenrobot.eventbus.EventBus;

public class TickEvents {
    private static final EventBus BUS = OrangeX.getInstance().getEventBus();

    public static void firePre() {
        BUS.post(new EventTickPre());
    }

    public static void firePost() {
        BUS.post(new EventTickPost());
    }
}
