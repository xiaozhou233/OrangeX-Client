package cn.xiaozhou233.orangex.nativebridge;

import java.io.File;

public class NativeBridge {
    public static String userHome = System.getProperty("user.home");
    public static void preload() {
        System.load(new File(userHome + "/.orangex/libOrangeXNative.dll").getAbsolutePath());
    }

    public static native boolean init();
    public static native void installNUpdateHook();
}