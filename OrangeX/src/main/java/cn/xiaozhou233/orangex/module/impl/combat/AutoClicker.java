package cn.xiaozhou233.orangex.module.impl.combat;

import cn.xiaozhou233.orangex.event.impl.EventTimerThread;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import net.minecraft.client.settings.KeyBinding;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoClicker extends Module {

    private final ScheduledExecutorService CLICK_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "OrangeX-ClickThread"));

    private final Random random = new Random();

    private long lastClickTime = System.currentTimeMillis();
    private long delayMs = 100;

    private final Deque<Double> intervalTicks = new ArrayDeque<>();
    private static final int HISTORY = 100;

    private final NumberValue cps =
            new NumberValue("CPS", "Clicks per second", 8d, 1, 20, 0.1);

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.COMBAT);
        addValue(cps);
    }

    private double getStdDev() {
        if (intervalTicks.size() < 5) return 1.0;

        double sum = 0;
        for (double v : intervalTicks) sum += v;
        double mean = sum / intervalTicks.size();

        double var = 0;
        for (double v : intervalTicks) var += (v - mean) * (v - mean);
        var /= intervalTicks.size();

        return Math.sqrt(var);
    }

    private long generateDelayMs() {
        double target = cps.getValue();
        double meanMs = 1000.0 / target;

        // 双峰：更快 / 更慢
        double shortDelay = meanMs * (0.65 + random.nextDouble() * 0.15); // 0.65~0.8 倍
        double longDelay = meanMs * (1.25 + random.nextDouble() * 0.35);  // 1.25~1.6 倍

        // 计算 stdDev
        double stdDev = getStdDev();

        // 10% 极端值
        if (random.nextDouble() < 0.1) {
            if (random.nextBoolean()) {
                return Math.max(1, (long) (meanMs * (0.35 + random.nextDouble() * 0.25)));
            } else {
                return Math.max(1, (long) (meanMs * (1.7 + random.nextDouble() * 0.8)));
            }
        }

        // 如果 stdDev < 1，则强制“极端化”
        if (stdDev < 1.0) {
            if (random.nextBoolean()) {
                return Math.max(1, (long) (meanMs * (0.35 + random.nextDouble() * 0.25)));
            } else {
                return Math.max(1, (long) (meanMs * (1.7 + random.nextDouble() * 0.8)));
            }
        }

        // 这里是关键：让“短间隔出现概率更高”，保证平均 CPS 不下降
        // 例如：短间隔 70% / 长间隔 30%
        boolean pickShort = random.nextDouble() < 0.7;
        return Math.max(1, (long) (pickShort ? shortDelay : longDelay));
    }

    @Subscribe
    public void onTimer(EventTimerThread event) {
        if (mc.theWorld == null) return;
        if (!Mouse.isButtonDown(0)) return;

        long now = System.currentTimeMillis();
        if (now - lastClickTime < delayMs) return;

        int keyCode = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, true);
        KeyBinding.onTick(keyCode);

        CLICK_EXECUTOR.schedule(() -> {
            KeyBinding.setKeyBindState(keyCode, false);
        }, 1 + random.nextInt(10), TimeUnit.MILLISECONDS);

        double intervalMs = now - lastClickTime;
        double intervalTick = intervalMs / 50.0;

        intervalTicks.addLast(intervalTick);
        if (intervalTicks.size() > HISTORY) intervalTicks.removeFirst();

        lastClickTime = now;
        delayMs = generateDelayMs();
    }

    @Override
    protected void onDisable() {
        CLICK_EXECUTOR.shutdownNow();
    }
}
