package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    @Subscribe
    public void onKey(EventKey event) {
        if (event.getKey() == Keyboard.KEY_RSHIFT) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGuiScreen());
        }
    }
}
