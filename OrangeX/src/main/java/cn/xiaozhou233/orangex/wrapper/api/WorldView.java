package cn.xiaozhou233.orangex.wrapper.api;

import cn.xiaozhou233.orangex.wrapper.common.WorldSnapshot;

/**
 * Cross-version view of the rendered world: camera state and nearby players.
 *
 * <p>Implementations read the game every time {@link #snapshot()} is called,
 * so the result must only be used for the frame that produced it. Feature
 * code (ESP, tracers, ...) should depend on this interface and on the
 * {@code wrapper.common} value objects only.</p>
 */
public interface WorldView {

    /**
     * Captures the camera and player list for the current frame.
     *
     * @return {@code null} while no level is loaded (title screen, loading).
     */
    WorldSnapshot snapshot();
}
