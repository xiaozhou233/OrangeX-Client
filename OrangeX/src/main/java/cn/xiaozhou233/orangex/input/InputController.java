package cn.xiaozhou233.orangex.input;

import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.event.impl.EventTickPost;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Drives {@link ClientInput}: handles {@code EventKey} from the EventBus and
 * reconciles the input screen state around the game tick.
 */
public final class InputController {

    private final Minecraft minecraft;
    private boolean failureLogged;

    public InputController(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    /**
     * Consumes key events posted by the injected input hooks. Only the toggle
     * key changes the input screen state; every event is still visible to
     * other subscribers.
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onKey(EventKey event) {
        try {
            ClientInput.onKey(minecraft, event.getKey());
        } catch (Throwable throwable) {
            logFailure(throwable);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTick(EventTickPre event) {
        try {
            ClientInput.tick(minecraft);
        } catch (Throwable throwable) {
            logFailure(throwable);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTickPost(EventTickPost event) {
        try {
            ClientInput.applyPending(minecraft);
        } catch (Throwable throwable) {
            logFailure(throwable);
        }
    }

    private void logFailure(Throwable throwable) {
        if (!failureLogged) {
            failureLogged = true;
            System.err.println("[OrangeX] Input tick failed");
            throwable.printStackTrace();
        }
    }
}
