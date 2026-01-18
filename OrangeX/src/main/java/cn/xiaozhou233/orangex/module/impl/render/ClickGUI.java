package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER);
        setKeyBind(Keyboard.KEY_RSHIFT);
    }

    @Override
    protected void onEnable() {
        super.onEnable();

//        mc.displayGuiScreen(new ClickGuiScreen());

        setEnabled(false);
    }
}
