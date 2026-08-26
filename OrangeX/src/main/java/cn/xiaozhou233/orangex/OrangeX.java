package cn.xiaozhou233.orangex;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import lombok.Getter;
import lombok.Setter;
import org.greenrobot.eventbus.EventBus;

@Getter
public class OrangeX {
    @Setter
    private RuntimeInfo runtime = null;
    public static final OrangeX INSTANCE = new OrangeX();

    private final EventBus eventBus = EventBus.builder()
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .build();


    public void start() {
        System.out.println("OrangeX starting...");
        if(runtime==null) {
            throw new RuntimeException("Runtime not set.");
        }
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