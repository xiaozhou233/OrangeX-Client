package cn.xiaozhou233.orangex.entry;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.runtime.RuntimeDetector;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;

public class Entry {
    public static void entry() {
        RuntimeInfo runtime = RuntimeDetector.detect();
        RuntimeDetector.debug(runtime);

        OrangeX.getInstance().setRuntime(runtime);
        OrangeX.getInstance().start();
    }
}
