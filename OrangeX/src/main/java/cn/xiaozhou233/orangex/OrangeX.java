package cn.xiaozhou233.orangex;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;

@Getter
public class OrangeX {
    public static final OrangeX INSTANCE = new OrangeX();
    private final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .build();


    public void start() {
        System.out.println("OrangeX starting...");

        System.out.println("OrangeX started.");
    }

    public void stop() {
        System.out.println("OrangeX stopping...");
        System.out.println("OrangeX stopped.");
    }

    public static OrangeX getInstance() {
        return INSTANCE;
    }
}