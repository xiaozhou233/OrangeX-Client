package cn.xiaozhou233.orangex.module.impl.misc;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.event.impl.EventWorldLoad;
import org.greenrobot.eventbus.Subscribe;

public class Test {
    @Subscribe
    public void onKey(EventKey event) {
        System.out.println("Key: " + event.getKey());
    }

    @Subscribe
    public void onWorldLoad(EventWorldLoad event) {
        System.out.println("World loaded");
    }

}
