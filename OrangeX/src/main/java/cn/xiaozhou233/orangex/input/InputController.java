package cn.xiaozhou233.orangex.input;

import cn.xiaozhou233.orangex.event.impl.EventTickPost;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Drives {@link ClientInput} from the game tick.
 */
public final class InputController {

    private final Minecraft minecraft;
    private boolean failureLogged;

    public InputController(Minecraft minecraft) {
        this.minecraft = minecraft;
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
