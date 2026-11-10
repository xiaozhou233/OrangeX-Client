package cn.xiaozhou233.orangex.modules.impl.movement;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import org.greenrobot.eventbus.Subscribe;

/**
 * Removes the client-side jump cooldown.
 *
 * <p>Vanilla keeps a short timer after every jump before the next one can
 * start. Clearing it before the player tick lets holding the jump key trigger
 * a new jump as soon as the player touches the ground again.</p>
 */
public class NoJumpDelay extends Module {

    private boolean warned;

    public NoJumpDelay() {
        super("NoJumpDelay", ModuleCategory.MOVEMENT);
    }

    @Subscribe
    public void onTickPre(EventTickPre event) {
        Player player = OrangeX.getInstance().getPlayer();
        if (player == null) {
            return;
        }

        try {
            player.clearJumpDelay();
        } catch (Throwable throwable) {
            // Do not retry a broken reflection lookup on every single tick.
            if (!warned) {
                warned = true;
                System.err.println("[NoJumpDelay] Failed to clear jump delay: "
                        + throwable.getMessage());
            }
        }
    }
}
