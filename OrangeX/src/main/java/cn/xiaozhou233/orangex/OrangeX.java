package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.font.FontManager;
import cn.xiaozhou233.orangex.mixin.MixinManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

@Getter
public class OrangeX {
    public static final OrangeX INSTANCE = new OrangeX();
    public MixinManager mixinManager = new MixinManager();
    FontManager fontManager = new FontManager();
    private final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .build();


    public void start() {
        System.out.println("OrangeX starting...");

        mixinManager.start();

        System.out.println("OrangeX started.");
    }

    public void stop() {
        System.out.println("OrangeX stopping...");
        System.out.println("OrangeX stopped.");
    }

    public static OrangeX getInstance() {
        return INSTANCE;
    }
}