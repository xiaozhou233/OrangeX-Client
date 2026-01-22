package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.alts.AltManager;
import cn.xiaozhou233.orangex.font.FontManager;
import cn.xiaozhou233.orangex.mixin.MixinManager;
import cn.xiaozhou233.orangex.module.ModuleManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

import java.awt.*;
import java.io.File;

public class OrangeX {
    @Getter
    private static File orangeXDir = new File(System.getProperty("user.home") + "/.orangex");
    private static OrangeX instance;
    @Getter
    private final EventBus eventBus;
    @Getter
    private final ModuleManager moduleManager;
    private static FontManager fontManager;
    @Getter
    private final AltManager altManager;

    public OrangeX() {
        this.eventBus = EventBus.builder()
                .logNoSubscriberMessages(false)
                .logSubscriberExceptions(false)
                .sendNoSubscriberEvent(false)
                .sendSubscriberExceptionEvent(false)
                .build();

        // Module Manager
        this.moduleManager = new ModuleManager();

        // Alt Manager
        this.altManager = new AltManager();

        eventBus.register(altManager);
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
