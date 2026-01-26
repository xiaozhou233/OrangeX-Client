package cn.xiaozhou233.orangex.manager;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventTimerThread;
import lombok.Getter;

@Getter
public class ThreadManager {

    private Thread timerThread;
    private volatile boolean running = false;

    private static final long TIMER_INTERVAL_MS = 10L;

    public void init() {
        if (running) {
            return;
        }

        running = true;

        timerThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                OrangeX.getInstance()
                        .getEventBus()
                        .post(new EventTimerThread());

                try {
                    Thread.sleep(TIMER_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "OrangeX-Timer");

        timerThread.start();

        Runtime.getRuntime().addShutdownHook(
                new Thread(this::shutdown, "OrangeX-Timer-Shutdown")
        );
    }

    public void shutdown() {
        running = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }
}
