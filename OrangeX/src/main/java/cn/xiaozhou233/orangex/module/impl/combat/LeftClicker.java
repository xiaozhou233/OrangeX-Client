package cn.xiaozhou233.orangex.module.impl.combat;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.option.impl.BooleanOption;
import cn.xiaozhou233.orangex.module.option.impl.DoubleOption;
import cn.xiaozhou233.orangex.module.option.impl.ModeOption;
import cn.xiaozhou233.orangex.event.impl.EventMouseButton;
import net.minecraft.client.settings.KeyBinding;
import org.greenrobot.eventbus.Subscribe;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class LeftClicker extends Module {
    private final DoubleOption minCPS = new DoubleOption("MinCPS", "", this, 8.0, 1.0, 20.0, 0.5, 1);
    private final DoubleOption maxCPS = new DoubleOption("MaxCPS", "", this, 12.0, 1.0, 20.0, 0.5, 1);
    private final BooleanOption breakBlocks = new BooleanOption("BreakBlocks", "", this, true);
    private final BooleanOption holdToClick = new BooleanOption("HoldToClick", "", this, true);
    private final ModeOption timingMode = new ModeOption("Timing", "", this, "Advanced", "Flat", "Legacy", "Advanced");

    private final Random random = new Random();
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final Object monitor = new Object();
    private Thread workerThread;
    private boolean leftHeld = false;
    private boolean activationWasHeld = false;
    private long activationStartTime = 0;
    private boolean breakingBlock = false;
    private boolean clicking = false;

    private long lastClickDelay = 0;
    private int burstClickCount = 0;
    private int burstLength = 0;
    private boolean burstActive = false;
    private boolean fastPhaseActive = true;
    private int fastPhaseClickCount = 0;
    private int slowPhaseClickCount = 0;
    private int configuredFastPhaseLength = 7 + new Random().nextInt(8);
    private int slowPhaseLength = 75 + new Random().nextInt(125);

    public LeftClicker() {
        super("AutoClicker", "Automatically clicks", ModuleCategory.COMBAT);
        addOption(minCPS);
        addOption(maxCPS);
        addOption(breakBlocks);
        addOption(holdToClick);
        addOption(timingMode);
    }

    @Override
    protected void onEnable() {
        active.set(true);
        clicking = false;
        activationWasHeld = false;
        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(this::workerLoop, "AutoClicker");
            workerThread.start();
        }
        synchronized (monitor) { monitor.notify(); }
    }

    @Override
    protected void onDisable() {
        active.set(false);
        if (clicking) releaseClick();
        leftHeld = false;
        breakingBlock = false;
    }

    private void workerLoop() {
        while (true) {
            try { Thread.sleep(5); } catch (InterruptedException e) { break; }

            synchronized (monitor) {
                while (!active.get()) {
                    try { monitor.wait(); } catch (InterruptedException e) { return; }
                }
            }

            try {
                runClickCycle();
            } catch (Exception ignored) {}
        }
    }

    private void runClickCycle() {
        if (mc.currentScreen != null) return;
        if (!mc.inGameHasFocus && mc.currentScreen == null) return;

        if (holdToClick.getValue() && !leftHeld) {
            activationWasHeld = false;
            if (clicking) releaseClick();
            return;
        }

        if (!activationWasHeld) {
            activationWasHeld = true;
            activationStartTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - activationStartTime < 50) return;

        if (breakBlocks.getValue() && mc.objectMouseOver != null &&
            mc.objectMouseOver.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!breakingBlock) {
                breakingBlock = true;
                if (!clicking) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                    clicking = true;
                }
            }
            return;
        }
        if (breakingBlock) {
            breakingBlock = false;
            releaseClick();
        }

        long clickDelay = calculateNextClickDelay() - 5;
        if (clickDelay <= 50) clickDelay = 45;

        double delayScale = (100.0 - Math.min(clickDelay, 99) + 45.0) / 100.0;
        double holdBiasPercent = 40.0 * delayScale;
        double releaseFraction = (30 + random.nextInt() % 10 + holdBiasPercent) / 100.0;
        long holdDuration = (long)(clickDelay * (1.0 - releaseFraction));
        long releaseDuration = (long)(clickDelay * releaseFraction);

        if (!leftHeld && holdToClick.getValue()) { activationWasHeld = false; return; }

        pressClick();
        try { Thread.sleep(Math.max(0, holdDuration)); } catch (InterruptedException e) { return; }

        releaseClick();
        try { Thread.sleep(Math.max(0, releaseDuration)); } catch (InterruptedException e) { return; }
    }

    private void pressClick() {
        if (!clicking) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            clicking = true;
        }
    }

    private void releaseClick() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        clicking = false;
    }

    private long calculateNextClickDelay() {
        int min = (int)Math.round(minCPS.getValue());
        int max = (int)Math.round(maxCPS.getValue());
        if (min > max) min = max;
        int range = max - min;
        int selected = range <= 0 ? min : random.nextInt(range) + min + 1;

        String mode = timingMode.getValue();
        if (mode.equals("Flat")) {
            lastClickDelay = 1000 / selected;
            return lastClickDelay;
        }

        if (selected == 0) selected = 1;

        if (mode.equals("Legacy")) {
            return calculateLegacyDelay(selected);
        }

        return calculateAdvancedDelay(min, max);
    }

    private long calculateLegacyDelay(int selectedCps) {
        lastClickDelay = 1000 / selectedCps;

        if (!burstActive) {
            if (random.nextInt(4) == 1) {
                burstActive = true;
                burstLength = 1 + random.nextInt(5);
            } else if (random.nextInt(10) == 1 && random.nextInt(10) == 1) {
                burstActive = true;
                burstLength = 5 + random.nextInt(10);
            }
        }
        if (burstActive && ++burstClickCount >= burstLength) {
            burstClickCount = 0;
            burstActive = false;
        }

        if (random.nextInt(48) % (fastPhaseActive ? 6 : 10) == 0 && !burstActive) {
            lastClickDelay += random.nextInt(45) + 40;
        }

        if (fastPhaseActive) {
            if (++fastPhaseClickCount >= configuredFastPhaseLength) {
                slowPhaseLength = 75 + random.nextInt(125);
                fastPhaseActive = false;
                fastPhaseClickCount = 0;
            }
            return lastClickDelay + (random.nextInt(5) == 3 ? 50 : 25);
        }

        if (++slowPhaseClickCount >= slowPhaseLength) {
            fastPhaseActive = true;
            configuredFastPhaseLength = 7 + random.nextInt(8);
            slowPhaseClickCount = 0;
        }
        return lastClickDelay;
    }

    private long calculateAdvancedDelay(int minCps, int maxCps) {
        double internalMin = minCps * 1.5;
        double internalMax = maxCps * 1.5;
        double target = internalMin + random.nextDouble() * (internalMax - internalMin);
        double noise = 0;
        for (int i = 0; i < 6; i++) noise += random.nextDouble();
        noise = (noise - 3) / 3.0;
        double cps = target + noise * (internalMax - internalMin) * 0.15;
        cps = Math.max(internalMin, Math.min(internalMax, cps));

        if (random.nextInt(100) < 12) cps *= 1.0 + random.nextDouble() * 0.05;
        if (random.nextInt(100) < 17) cps *= 1.0 + random.nextDouble() * 0.08;

        lastClickDelay = (long)(1000.0 / cps);

        if (random.nextDouble() < 0.06) {
            return lastClickDelay;
        }

        lastClickDelay += 30 + random.nextInt(11);
        return lastClickDelay;
    }

    @Subscribe
    public void onMouseButton(EventMouseButton event) {
        if (event.getButton() == 0) {
            leftHeld = event.isPressed();
        }
    }
}