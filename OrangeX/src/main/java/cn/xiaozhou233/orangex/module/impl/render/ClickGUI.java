package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.ClickGuiScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER);
        keyBind.setKey(Keyboard.KEY_RSHIFT);

    }

    @Override
    protected void onEnable() {
        super.onEnable();

        mc.displayGuiScreen(new ClickGuiScreen());

        setEnabled(false);
    }
}
