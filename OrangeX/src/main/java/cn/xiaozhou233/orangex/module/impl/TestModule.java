package cn.xiaozhou233.orangex.module.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.option.impl.*;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;

import java.awt.event.KeyEvent;

public class TestModule extends Module {
    public TestModule() {
        super("Test", "A test module", ModuleCategory.MISC);

        addOption(new BooleanOption("boolean", "Boolean option", this, true));
        addOption(new DoubleOption("double", "Double slider", this, 0.5, 0.0, 1.0, 0.05, 2));
        addOption(new IntOption("int", "Int slider", this, 10, 1, 100, 5));
        addOption(new ModeOption("mode", "Mode selector", this, "A", "A", "B", "C"));
        addOption(new ColorOption("color", "ARGB color", this, 0xFFFF0000));
        addOption(new StringOption("string", "Text input", this, "hello", 32));
    }

    @Override
    protected void onEnable() {
        System.out.println("[OrangeX] TestModule enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("[OrangeX] TestModule disabled");
    }

    @Subscribe
    public void onKey(KeyEvent event) {

    }
}