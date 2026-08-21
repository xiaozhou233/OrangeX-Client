package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.ClickGUIScreen;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    public ClickGUI() {
        super("ClickGUI", "ClickGUI", ModuleCategory.RENDER);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    protected void onEnable() {
        mc.displayGuiScreen(new ClickGUIScreen());
    }

    @Subscribe
    public void onKey(EventKey eventKey) {
        // unused, but necessary for eventbus
    }
}
