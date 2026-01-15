package cn.xiaozhou233.orangex.utils;

import java.lang.reflect.Method;

public final class JuiceLoaderFacade {

    private static final Class<?> JL;
    private static final Method INIT;
    private static final Method GET_CLASS_BYTES;
    private static final Method REDEFINE_CLASS;
    private static final Method GET_LOADED_CLASSES;

    static {
        try {
            JL = Class.forName(
                    "cn.xiaozhou233.juiceloader.JuiceLoader",
                    true,
                    ClassLoader.getSystemClassLoader()
            );

            INIT = JL.getMethod("init");
            GET_CLASS_BYTES = JL.getMethod("getClassBytes", Class.class);
            REDEFINE_CLASS = JL.getMethod(
                    "redefineClass",
                    Class.class,
                    byte[].class,
                    int.class
            );
            GET_LOADED_CLASSES = JL.getMethod("getLoadedClasses");

        } catch (Exception e) {
            throw new RuntimeException("Failed to init JuiceFacade", e);
        }
    }

    private JuiceLoaderFacade() {}

    public static byte[] getClassBytes(Class<?> clazz) {
        try {
            return (byte[]) GET_CLASS_BYTES.invoke(null, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean redefineClass(Class<?> clazz, byte[] bytes) {
        try {
            return (boolean) REDEFINE_CLASS.invoke(null, clazz, bytes, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?>[] getLoadedClasses() {
        try {
            return (Class<?>[]) GET_LOADED_CLASSES.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
