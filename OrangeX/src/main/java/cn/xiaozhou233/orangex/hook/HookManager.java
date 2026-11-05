package cn.xiaozhou233.orangex.hook;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.hook.impl.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class HookManager {

    @Getter
    private static final HookManager INSTANCE = new HookManager();

    private final List<ClassHook> hooks = new ArrayList<ClassHook>();

    private HookManager() {
        register(new TickHook());
        register(new Render2DHook());
    }

    public void register(ClassHook hook) {
        hooks.add(hook);
    }

    public void applyAll() {
        ClassLoader loader = OrangeX.getInstance().getRuntimeInfo().getMinecraftClassLoader();

        for (ClassHook hook : hooks) {
            String className = hook.getTargetClass();
            if (className == null) {
                continue;
            }

            try {
                byte[] classBytes = JuiceAgentBridge.getClassBytesByName(
                        className.replace(".", "/"));
                if (classBytes == null) {
                    continue;
                }

                byte[] bytes = hook.transform(loader, classBytes);
                JuiceAgentBridge.retransformClassByName(className, bytes);
            } catch (Throwable t) {
                System.err.println("[OrangeX] Failed to apply hook: " + className);
                t.printStackTrace();
            }
        }
    }
}
