package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.mixin.MixinManager;
import cn.xiaozhou233.orangex.module.ModuleManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

public class OrangeX {
    @Getter
    public static final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .installDefaultEventBus();

    public static void start() {
        System.out.println("OrangeX loading...");

        MixinManager.start();

        ModuleManager.registerAll();
    }
}
