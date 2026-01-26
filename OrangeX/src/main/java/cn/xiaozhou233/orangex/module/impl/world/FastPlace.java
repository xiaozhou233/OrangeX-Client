package cn.xiaozhou233.orangex.module.impl.world;

import cn.xiaozhou233.orangex.event.impl.EventTick;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.utils.ReflectionUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.greenrobot.eventbus.Subscribe;

import java.util.Random;

public class FastPlace extends Module {

    // Level 1~5
    private final NumberValue level =
            new NumberValue("Level", "FastPlace level", 1d, 1d, 5d, 1d);

    private final Random random = new Random();

    public FastPlace() {
        super("FastPlace", ModuleCategory.WORLD);
        addValue(level);
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack == null || !(stack.getItem() instanceof ItemBlock)) return;

        try {
            int lvl = level.getValue().intValue();

            // Level -> min/max ms
            int minMs = (lvl - 1) * 50;
            int maxMs = lvl * 50;

            // Random ms within level range
            int delayMs = minMs + random.nextInt(maxMs - minMs + 1);

            int targetTick = (int) Math.ceil(delayMs / 50.0);
            int current = ReflectionUtil.getRightClickDelay();

            if (current > targetTick) {
                ReflectionUtil.setRightClickDelay(targetTick);
            }
        } catch (Exception ignored) {
        }
    }
}
