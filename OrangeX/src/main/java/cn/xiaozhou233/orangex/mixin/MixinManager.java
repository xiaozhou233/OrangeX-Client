package cn.xiaozhou233.orangex.mixin;

import cn.xiaozhou233.orangex.mixin.impl.MixinEntityRenderer;
import cn.xiaozhou233.orangex.mixin.impl.MixinGuiIngame;
import cn.xiaozhou233.orangex.mixin.impl.MixinMinecraft;
import cn.xiaozhou233.orangex.utils.JuiceLoaderFacade;
import cn.yapeteam.ymixin.Transformer;
import cn.yapeteam.ymixin.YMixin;

public final class MixinManager {

    private MixinManager() {}

    public static void start() {

        ClassLoader cl = MixinManager.class.getClassLoader();

        YMixin.init(
                className -> {
                    try {
                        return Class.forName(
                                className.replace("/", "."),
                                false,
                                cl
                        );
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                },
                clazz -> {
                    byte[] classBytes = JuiceLoaderFacade.getClassBytes(clazz);
                    if (classBytes == null) {
                        throw new RuntimeException(
                                "ClassBytes null: " + clazz.getName()
                        );
                    }
                    return classBytes;
                },
                null,
                null,
                null
        );

        Transformer transformer = new Transformer();
        try {
            transformer.addMixin(MixinGuiIngame.class);
            transformer.addMixin(MixinMinecraft.class);
            transformer.addMixin(MixinEntityRenderer.class);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to add mixin", e);
        }

        transformer.transform().forEach((name, bytes) -> {
            try {
                Class<?> targetClass = Class.forName(name, false, cl);
                boolean ok = JuiceLoaderFacade.redefineClass(targetClass, bytes);
                if (!ok) {
                    throw new RuntimeException("Redefine failed: " + name);
                }
                System.out.println("[OrangeX] Redefined class: " + name);
            } catch (Throwable e) {
                throw new RuntimeException(
                        "Failed to redefine class: " + name,
                        e
                );
            }
        });
    }
}
