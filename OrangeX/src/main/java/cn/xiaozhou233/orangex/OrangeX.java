package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.hook.HookManager;
import cn.xiaozhou233.orangex.input.InputController;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.modules.ModuleManager;
import cn.xiaozhou233.orangex.render.FontDebugOverlay;
import cn.xiaozhou233.orangex.render.Render2DManager;
import cn.xiaozhou233.orangex.runtime.RuntimeDetector;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.MinecraftFactory;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
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
    @Getter
    private Render2DManager render2DManager;
    @Getter
    private ModuleManager moduleManager = new ModuleManager();
    @Getter
    private Minecraft minecraft;
    @Getter
    private WorldView worldView;

    public OrangeX() {
        System.out.println("OrangeX <init> start!");
    }

    public void start() {
        System.out.println("OrangeX start!");

        runtimeInfo = RuntimeDetector.detect();
        RuntimeDetector.debug(runtimeInfo);

        MappingManager.init(runtimeInfo);

        Minecraft minecraft = MinecraftFactory.create(runtimeInfo, MappingManager.getResolver());
        this.minecraft = minecraft;
        System.out.println(minecraft.getMinecraft());

        try {
            this.worldView = MinecraftFactory.createWorld(
                    runtimeInfo, MappingManager.getResolver(), minecraft);
        } catch (Throwable throwable) {
            // The client must keep running even when a version cannot expose
            // world data; world-dependent modules simply stay inactive.
            System.err.println("[OrangeX] World wrapper unavailable: "
                    + throwable.getMessage());
            throwable.printStackTrace();
        }

        render2DManager = new Render2DManager(
                runtimeInfo, MappingManager.getResolver(), minecraft.getMinecraft());
        System.out.println("[OrangeX] Font backend: " + render2DManager.getBackendName());

        //eventBus.register(new FontDebugOverlay());
        eventBus.register(new InputController(minecraft));
        eventBus.register(this);

        moduleManager.init();

        HookManager.getINSTANCE().applyAll();
    }

    public void stop() {
        System.out.println("OrangeX stop!");
    }

    @Subscribe
    public void onKey(EventKey event) {
        System.out.println("KEY: " + event.getKey());
    }
}
