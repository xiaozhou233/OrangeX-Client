package cn.xiaozhou233.orangex.mixin;

import cn.xiaozhou233.orangex.utils.IOUtils;
import cn.xiaozhou233.orangex.mixin.impl.*;
import cn.xiaozhou233.orangex.utils.JuiceAgentFacade;
import cn.yapeteam.ymixin.Transformer;
import cn.yapeteam.ymixin.YMixin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class MixinManager {

    public void start() {

        ClassLoader cl = MixinManager.class.getClassLoader();

        // Check Obfuscation
        String content = null;
        try {
            Class.forName("net.minecraft.client.Minecraft");
        } catch (ClassNotFoundException e) {
            System.out.println("[OrangeX] Obfuscation detected, use mapping.");
            InputStream stream = MixinManager.class.getResourceAsStream("/joined.srg");

            try {
                content = new String(
                        IOUtils.toByteArray(stream),
                        StandardCharsets.UTF_8
                );
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

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
                    byte[] classBytes = JuiceAgentFacade.getClassBytes(clazz);
                    if (classBytes == null) {
                        throw new RuntimeException(
                                "ClassBytes null: " + clazz.getName()
                        );
                    }
                    return classBytes;
                },
                null,
                null,
                content
        );

        Transformer transformer = new Transformer();
        try {
            transformer.addMixin(MixinMinecraft.class);
            transformer.addMixin(MixinGuiIngame.class);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to add mixin", e);
        }

        transformer.transform().forEach((name, bytes) -> {
            try {
                Class<?> targetClass = Class.forName(name, false, cl);
                boolean ok = JuiceAgentFacade.retransformClass(targetClass, bytes);
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
