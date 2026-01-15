package cn.xiaozhou233.juiceagent.injector;

public class InjectorNative {
    /*
     * @param pid target process id
     */
    public native boolean inject(int pid, String path);

    /*
     * @param pid target process id
     * @param path injection dll path
     * @param configPath config file path
     */
    public native boolean inject(int pid, String path, String configPath);

    /*
     *
     */
    public static native WindowInfo[] findWindowsByTitle(String keyword);


    public static class WindowInfo {
        public String title;
        public int pid;

        public WindowInfo(String title, int pid) {
            this.title = title;
            this.pid = pid;
        }

        @Override
        public String toString() {
            return String.format("标题: %-30s | PID: %d", title, pid);
        }
    }
}