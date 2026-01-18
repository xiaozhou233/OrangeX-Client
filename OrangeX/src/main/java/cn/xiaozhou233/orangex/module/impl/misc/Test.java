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

    private final BooleanValue enabled = new BooleanValue("Enabled", "Toggle autoclicker", false);
    private final NumberValue cps = new NumberValue("CPS", "Clicks per second", 10.0, 1, 20, 1);
    private final KeybindValue bind = new KeybindValue("Bind", "Toggle key", 0);

    public Test() {
        super("Test", ModuleCategory.MISC);
        setEnabled(true);

        addValue(enabled);
        addValue(cps);
        addValue(bind);
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
