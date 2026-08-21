package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.ClickGUIScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    public ClickGUI() {
        super("ClickGUI", "ClickGUI", ModuleCategory.RENDER);
        setEnabled(false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    protected void onEnable() {
        mc.displayGuiScreen(new ClickGUIScreen());
        setEnabled(false);
    }
}