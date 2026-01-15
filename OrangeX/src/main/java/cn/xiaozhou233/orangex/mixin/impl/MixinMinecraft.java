package cn.xiaozhou233.orangex.mixin.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.*;
import cn.yapeteam.ymixin.annotations.Inject;
import cn.yapeteam.ymixin.annotations.Local;
import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.annotations.Target;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    int i = 0;

    @Inject(method = "runGameLoop", desc = "()V", target = @Target("HEAD"))
    private void onLoop() {
        OrangeX.getEventBus().post(new EventLoop());
    }

    @Inject(method = "runTick", desc = "()V", target = @Target("HEAD"))
    public void onTick() {
        OrangeX.getEventBus().post(new EventTick());
    }

    @Inject(method = "shutdownMinecraftApplet", desc = "()V", target = @Target("HEAD"))
    public void onShutdown() {
        OrangeX.stop();
    }

    @Inject(
            method = "runTick", desc = "()V",
            target = @Target(
                    value = "INVOKESTATIC",
                    target = "org/lwjgl/input/Keyboard.getEventKeyState()Z",
                    shift = Target.Shift.AFTER
            )
    )
    public void onKey(@Local(source = "key", index = 1) int key) {
        if (Minecraft.getMinecraft().currentScreen == null && Keyboard.getEventKeyState())
            OrangeX.getEventBus().post(new EventKey(key));
    }

    @Inject(method = "loadWorld", desc = "(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", target = @Target("HEAD"))
    public void onLoadWorld() {
        OrangeX.getEventBus().post(new EventWorldLoad());
    }
}
