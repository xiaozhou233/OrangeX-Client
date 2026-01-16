package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.impl.misc.Test;
import cn.xiaozhou233.orangex.module.impl.render.*;

public class ModuleManager {
    public static void registerAll() {
        // Render
        OrangeX.getEventBus().register(new HUD());
        OrangeX.getEventBus().register(new ClickGUI());

        // Misc
        OrangeX.getEventBus().register(new Test());
    }
}
