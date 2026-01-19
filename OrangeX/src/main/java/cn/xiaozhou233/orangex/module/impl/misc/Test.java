package cn.xiaozhou233.orangex.module.impl.misc;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.event.impl.EventWorldLoad;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import org.greenrobot.eventbus.Subscribe;

public class Test extends Module{

    public Test() {
        super("Test", ModuleCategory.MISC);

        addValue(new BooleanValue("Example Boolean1", "Example", true));
        addValue(new BooleanValue("Example Boolean2", "Example", false));
    }

    @Subscribe
    public void onKey(EventKey event) {
        System.out.println("Key: " + event.getKey());
    }

    @Subscribe
    public void onWorldLoad(EventWorldLoad event) {
        System.out.println("World loaded");
    }

}
