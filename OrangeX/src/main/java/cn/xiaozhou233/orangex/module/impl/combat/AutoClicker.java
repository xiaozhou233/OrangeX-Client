package cn.xiaozhou233.orangex.module.impl.combat;

import cn.xiaozhou233.orangex.event.impl.EventTick;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.concurrent.ThreadLocalRandom;

public class AutoClicker extends Module {

    private final NumberValue cps =
            new NumberValue("CPS", "Clicks per second", 12.0, 1, 30, 1);

    // jitter range (±)
    private final NumberValue jitter =
            new NumberValue("Jitter", "CPS random offset", 1.0, 0.0, 3.0, 0.1);

    // ignore block (deprecated now, but kept for compatibility)
    private final BooleanValue ignoreBlock =
            new BooleanValue("IgnoreBlock", "Ignore block click", false);

    private int tickCounter = 0;
    private boolean clicking = false;
    private double currentTicksPerClick = 0.0;

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.COMBAT);
        addValue(cps);
        addValue(jitter);
        addValue(ignoreBlock);
    }

    @Subscribe
    public void onTick(EventTick event) {

        if (!isEnabled() || mc.currentScreen != null || !Mouse.isButtonDown(0)) {
            reset();
            return;
        }

        MovingObjectPosition target = mc.objectMouseOver;

        // Target is a block
        if (target != null && target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

            tickCounter = 0;
            clicking = false;
            currentTicksPerClick = 0;
            return;
        }

        // If ignoreBlock enabled and looking at block -> skip clicking (optional)
        if (ignoreBlock.getValue() && target != null &&
                target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            reset();
            return;
        }

        if (currentTicksPerClick <= 0) {
            recalcDelay();
        }

        tickCounter++;

        if (tickCounter >= currentTicksPerClick) {
            // press
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindAttack.getKeyCode(), true);
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

            clicking = true;
            tickCounter = 0;

            // next click uses new random cps
            recalcDelay();
        } else if (clicking) {
            // release next tick
            releaseKey();
            clicking = false;
        }
    }

    private void recalcDelay() {
        double base = cps.getValue();
        double range = jitter.getValue();

        double randomCps = base + ThreadLocalRandom.current().nextDouble(-range, range);

        // clamp
        randomCps = Math.max(1.0, Math.min(20.0, randomCps));

        currentTicksPerClick = 20.0 / randomCps;
    }

    private void reset() {
        releaseKey();
        tickCounter = 0;
        clicking = false;
        currentTicksPerClick = 0;
    }

    @Override
    public void onDisable() {
        reset();
    }

    private void releaseKey() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindAttack.getKeyCode(), false);
    }
}
