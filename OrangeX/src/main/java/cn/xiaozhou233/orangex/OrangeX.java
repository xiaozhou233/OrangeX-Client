package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.hook.HookManager;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.runtime.RuntimeDetector;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.MinecraftFactory;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class OrangeX {
    @Getter
    private static final OrangeX instance = new OrangeX();
    @Getter
    private final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .build();
    @Getter
    private RuntimeInfo runtimeInfo;

    public OrangeX() {
        System.out.println("OrangeX <init> start!");
    }

    public void start() {
        System.out.println("OrangeX start!");

        runtimeInfo = RuntimeDetector.detect();
        RuntimeDetector.debug(runtimeInfo);

        MappingManager.init(runtimeInfo);

        Minecraft minecraft = MinecraftFactory.create(runtimeInfo, MappingManager.getResolver());
        System.out.println(minecraft.getMinecraft());

        // DELETE THIS ON RELEASE:
        eventBus.register(this);

        HookManager.getINSTANCE().applyAll();
    }

    public void stop() {
        System.out.println("OrangeX stop!");
    }

    @Subscribe()
    public void onTick(EventTickPre event) {
        System.out.println("PRE EVENT!");
    }
}
