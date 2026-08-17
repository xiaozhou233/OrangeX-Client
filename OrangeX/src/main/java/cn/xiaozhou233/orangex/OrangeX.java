package cn.xiaozhou233.orangex;

public class OrangeX {
    public static final OrangeX INSTANCE = new OrangeX();

    public void start() {
        System.out.println("OrangeX started.");
    }

    public void stop() {
        System.out.println("OrangeX stopped.");
    }

    public static OrangeX getInstance() {
        return INSTANCE;
    }
}