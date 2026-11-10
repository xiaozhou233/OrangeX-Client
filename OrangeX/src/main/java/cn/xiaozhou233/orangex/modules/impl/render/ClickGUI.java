package cn.xiaozhou233.orangex.modules.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.input.KeyCodes;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.ClickGuiRender;
import org.greenrobot.eventbus.Subscribe;

/**
 * Host module for the ClickGUI.
 *
 * <p>RSHIFT is the default keybind. Enabling the module opens the empty input
 * screen and disabling it closes the screen again. Draw the actual GUI in
 * {@link #onRender2D(EventRender2D)}.</p>
 */
public class ClickGUI extends Module {

    private boolean justEnabled;

    public ClickGUI() {
        super("ClickGUI", ModuleCategory.RENDER, KeyCodes.RSHIFT);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (!mc.isInWorld() || mc.isScreenOpen()) {
            // The input screen cannot open right now (title screen, chat,
            // inventory, ...); do not leave the module in a half-enabled state.
            setEnabled(false);
            return;
        }
        justEnabled = true;
        ClickGuiRender.reset();
        // Defer opening to EventTickPost: opening inside the keyboard callback
        // would let the same RSHIFT event reach the new screen and close it.
        ClientInput.requestOpen();
    }

    @Override
    protected void onDisable() {
        justEnabled = false;
        ClickGuiRender.reset();
        ClientInput.requestClose();
        if (OrangeX.getInstance().getConfigManager() != null) {
            OrangeX.getInstance().getConfigManager().saveIfDirty();
        }
        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTickPre event) {
        if (justEnabled) {
            justEnabled = false;
            return;
        }
        if (!ClientInput.isOpen()) {
            // The screen was closed by vanilla handling (for example ESC);
            // keep the module state in sync instead of requiring two RSHIFT
            // presses to open it again.
            setEnabled(false);
        }
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        if (!ClientInput.isOpen()) {
            return;
        }

        ClickGuiRender.render(event);
    }
}
