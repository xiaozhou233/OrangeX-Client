package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.impl.render.HUD;

public class ModuleManager {
    public static void registerAll() {
        OrangeX.getEventBus().register(new HUD());
    }
}
