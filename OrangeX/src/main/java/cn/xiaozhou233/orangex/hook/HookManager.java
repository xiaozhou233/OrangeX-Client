package cn.xiaozhou233.orangex.hook;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.hook.impl.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HookManager {

    @Getter
    private static final HookManager INSTANCE = new HookManager();

    /**
     * Original (pre-injection) bytes of every hook target, keyed by dot name.
     * Used by 1.8.9 screen generation so it never has to retransform a class
     * that has already been patched.
     */
    private static final Map<String, byte[]> TARGET_BYTES =
            new ConcurrentHashMap<String, byte[]>();

    private final List<ClassHook> hooks = new ArrayList<ClassHook>();

    private HookManager() {
        register(new TickHook());
        register(new Render2DHook());
        register(new GuiInputHook());
        register(new KeyPressHook());
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
                TARGET_BYTES.put(className, classBytes);

                byte[] bytes = hook.transform(loader, classBytes);
                JuiceAgentBridge.retransformClassByName(className, bytes);
            } catch (Throwable t) {
                System.err.println("[OrangeX] Failed to apply hook: " + className);
                t.printStackTrace();
            }
        }
    }

    public static byte[] getTargetBytes(String className) {
        return TARGET_BYTES.get(className);
    }
}
