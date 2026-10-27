package cn.xiaozhou233.juiceagent.injector;

public class WindowInfo {
    public String title;
    public int pid;

    public WindowInfo(String title, int pid) {
        this.title = title;
        this.pid = pid;
    }

    @Override
    public String toString() {
        return String.format("Title: %-30s | PID: %d", title, pid);
    }
}
