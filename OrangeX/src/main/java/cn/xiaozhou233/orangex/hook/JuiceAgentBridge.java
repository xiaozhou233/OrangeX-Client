package cn.xiaozhou233.orangex.hook;

import java.lang.reflect.Method;

/**
 * Reflective access to the JuiceAgent API.
 *
 * <p>The agent registers its native methods on the API class loaded by the
 * system class loader. On Fabric, OrangeX itself is loaded by Knot, so a
 * direct reference could resolve to a second copy of {@code JuiceAgent} that
 * has no native implementation and throws {@link UnsatisfiedLinkError}.
 * Looking the class up through the system class loader always reaches the
 * registered copy.</p>
 */
final class JuiceAgentBridge {

    private static final String API_CLASS = "cn.xiaozhou233.juiceagent.api.JuiceAgent";

    private static Method getClassBytesByName;
    private static Method retransformClassByName;

    static byte[] getClassBytesByName(String className) {
        init();

        try {
            return (byte[]) getClassBytesByName.invoke(null, className);
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "JuiceAgent.getClassBytesByName failed: " + className, t);
        }
    }

    static void retransformClassByName(String className, byte[] bytes) {
        init();

        try {
            retransformClassByName.invoke(null, className, bytes, bytes.length);
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "JuiceAgent.retransformClassByName failed: " + className, t);
        }
    }

    private static synchronized void init() {
        if (getClassBytesByName != null) {
            return;
        }

        try {
            Class<?> api = Class.forName(API_CLASS, true, ClassLoader.getSystemClassLoader());
            getClassBytesByName = api.getMethod("getClassBytesByName", String.class);
            retransformClassByName = api.getMethod(
                    "retransformClassByName", String.class, byte[].class, int.class);
        } catch (Throwable t) {
            throw new IllegalStateException("JuiceAgent API is not available", t);
        }
    }

    private JuiceAgentBridge() {
    }
}
