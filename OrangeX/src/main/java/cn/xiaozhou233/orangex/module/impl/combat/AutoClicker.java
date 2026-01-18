package cn.xiaozhou233.orangex.module.impl.combat;

import cn.xiaozhou233.orangex.event.impl.EventTick;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import net.minecraft.client.settings.KeyBinding;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AutoClicker extends Module {
    private final NumberValue cps = new NumberValue("CPS", "Clicks per second", 12.0, 1, 30, 1);

    private long lastClickTime = 0L;

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.COMBAT, Keyboard.KEY_F);
        addValue(cps);
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (!isEnabled()) {
            releaseKey();
            return;
        }

        if (mc.currentScreen != null) {
            releaseKey();
            return;
        }

        // Must hold physical left mouse button
        if (!Mouse.isButtonDown(0)) {
            releaseKey();
            return;
        }

        long now = System.currentTimeMillis();
        long delay = (long) (1000.0 / cps.getValue());

        if (now - lastClickTime >= delay) {
            // Simulate one left click
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindAttack.getKeyCode(),
                    true
            );
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

            lastClickTime = now;
        } else {
            // Release immediately to avoid block breaking
            releaseKey();
        }
    }

    @Override
    public void onDisable() {
        releaseKey();
    }

    private void releaseKey() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindAttack.getKeyCode(),
                false
        );
    }
}
