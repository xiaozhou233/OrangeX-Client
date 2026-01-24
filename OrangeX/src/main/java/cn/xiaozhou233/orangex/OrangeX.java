package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.alts.AltManager;
import cn.xiaozhou233.orangex.font.FontManager;
import cn.xiaozhou233.orangex.manager.ResourceManager;
import cn.xiaozhou233.orangex.mixin.MixinManager;
import cn.xiaozhou233.orangex.module.ModuleManager;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

@Getter
public class OrangeX {
    @Getter
    private static OrangeX instance = new OrangeX();

    private final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .build();
    private final ResourceManager resourceManager = new ResourceManager();
    private final MixinManager mixinManager = new MixinManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final FontManager fontManager = new FontManager();
    private final AltManager altManager = new AltManager();

    public OrangeX() {
    }

    // Invoke by Loader/MCPEntry
    // See Loader -> cn.xiaozhou233.orangex.loader.Loader
    // See Minecraft -> cn.xiaozhou233.MCPEntry
    public void start() {
        moduleManager.init();
        altManager.init();
        mixinManager.start();
    }

    // Invoke by Minecraft
    // See Mixin cn.xiaozhou233.orangex.mixin.impl.MixinMinecraft.onShutdown()
    public void stop() {
        System.out.println("OrangeX stopping...");
    }

}
