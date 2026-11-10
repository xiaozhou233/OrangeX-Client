package cn.xiaozhou233.orangex.wrapper.api;

/**
 * Cross-version view of the local player.
 *
 * <p>Only capabilities that need version-specific reflection live here. The
 * implementation reads the current player from the client on every call, so a
 * wrapper instance survives world changes and reconnects.</p>
 */
public interface Player {

    /**
     * Clears the client-side jump cooldown.
     *
     * <p>Vanilla only starts a new jump when the local jump timer has run out,
     * which is why holding the jump key produces a fixed jump interval. Zeroing
     * the timer every tick removes that interval; the server still receives
     * normal jump packets, so this is a client-side timing change only.</p>
     *
     * <p>Does nothing while no world/player is loaded.</p>
     */
    void clearJumpDelay();
}
