package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.font.FontManager;
import cn.xiaozhou233.orangex.mixin.MixinManager;
import cn.xiaozhou233.orangex.module.ModuleManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class OrangeX {
    private static OrangeX instance;
    @Getter
    private final EventBus eventBus;
    @Getter
    private final ModuleManager moduleManager;
    private static FontManager fontManager;

    public OrangeX() {
        // EventBus
        this.eventBus = EventBus.builder()
                .logNoSubscriberMessages(false)
                .logSubscriberExceptions(false)
                .sendNoSubscriberEvent(false)
                .sendSubscriberExceptionEvent(false)
                .installDefaultEventBus();

        // Module Manager
        this.moduleManager = new ModuleManager();
    }

    public void start() {
        System.out.println("OrangeX start!");
        MixinManager.start();
        moduleManager.init();
    }

    public void stop() {
        System.out.println("OrangeX stopping...");
    }

    public static FontManager getFontManager() {
        if (fontManager == null) {
            fontManager = new FontManager();
        }
        return fontManager;
    }

    public static OrangeX getInstance() {
        if (instance == null) {
            instance = new OrangeX();
        }
        return instance;
    }
}
