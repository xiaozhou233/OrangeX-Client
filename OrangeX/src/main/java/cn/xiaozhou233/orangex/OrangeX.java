package cn.xiaozhou233.orangex;

import cn.xiaozhou233.orangex.mixin.MixinManager;

public class OrangeX {
    public static final OrangeX INSTANCE = new OrangeX();
    public MixinManager mixinManager = new MixinManager();

    public void start() {
        System.out.println("OrangeX starting...");

        mixinManager.start();

        System.out.println("OrangeX started.");
    }

    public void stop() {
        System.out.println("OrangeX stopped.");
    }

    public static OrangeX getInstance() {
        return INSTANCE;
    }
}