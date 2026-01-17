package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.font.FontManager;
import cn.xiaozhou233.orangex.mixin.MixinManager;
import cn.xiaozhou233.orangex.module.ModuleManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class OrangeX {
    @Getter
    private static final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .installDefaultEventBus();
    @Getter
    private static ModuleManager moduleManager = new ModuleManager();
    @Getter
    private static String orangexDir = new File(System.getProperty("user.dir") + "/.orangex/").getAbsolutePath();
    private static FontManager fontManager;

    public static void start() {
        System.out.println("OrangeX loading...");

        MixinManager.start();

    }

    public static FontManager getFontManager() {
        if (fontManager == null) {
            fontManager = new FontManager();
        }
        return fontManager;
    }

    public static void stop() {
        System.out.println("OrangeX stopping...");
    }
}
