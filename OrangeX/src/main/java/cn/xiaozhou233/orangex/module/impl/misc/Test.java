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
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class Test extends Module{

    public Test() {
        super("Test", ModuleCategory.MISC);

        // Boolean Examples
        addValue(new BooleanValue("Example Boolean1", "Example", true));
        addValue(new BooleanValue("Example Boolean2", "Example", false));

        // Keybinding Examples
        addValue(new KeybindValue("Example1", "Example", 0));
        addValue(new KeybindValue("Example2", "Example", Keyboard.KEY_RSHIFT));

        // Mode Examples
        addValue(new ModeValue("Example Mode", "Example", "ModeA", new ArrayList<>(Arrays.asList("ModeA", "ModeB", "ModeC"))));

        // Number Examples
        addValue(new NumberValue("Example Number", "Example", 0.0, 0, 100, 0.1));

        addValue(keyBind);
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
