package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.runtime.RuntimeDetector;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.MinecraftFactory;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import lombok.Getter;

public class OrangeX {
    @Getter
    private static final OrangeX instance = new OrangeX();

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
    }

    public void stop() {
        System.out.println("OrangeX stop!");
    }
}
