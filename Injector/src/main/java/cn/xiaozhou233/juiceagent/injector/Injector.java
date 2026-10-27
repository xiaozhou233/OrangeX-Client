package cn.xiaozhou233.juiceagent.injector;

public class Injector {
    /*
     * @param pid process id
     * @param path libinject path
     */
    public native static boolean inject(int pid, String path);

    /*
     * @param pid process id
     * @param path libinject path
     * @param configDir config directory
     */
    public native static boolean inject(int pid, String path, String configDir);

    /*
     * @param keyword window title keyword
     * @return windows with title containing keyword
     */
    public static native WindowInfo[] findWindowsByTitle(String keyword);
}