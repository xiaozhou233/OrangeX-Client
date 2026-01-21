package cn.xiaozhou233.orangex.ui.notice;

public class Notice {
    public String title;
    public String content;
    public long startTime;
    public long duration;

    public int bgColor;

    public Notice(String title, String content, long duration, int bgColor) {
        this.title = title;
        this.content = content;
        this.duration = duration;
        this.bgColor = bgColor;
        this.startTime = System.currentTimeMillis();
    }
}
