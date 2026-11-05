package cn.xiaozhou233.orangex.render;

import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Optional debug overlay. Enable with {@code -Dorangex.debugFont=true}.
 */
public final class FontDebugOverlay {

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onRender2D(EventRender2D event) {
        Render2D render2D = event.getRender2D();
        FontRenderer title = render2D.getFont(14.0f, true);
        FontRenderer body = render2D.getFont(10.0f, false);

        title.drawStringWithShadow("OrangeX", 8, 8, 0xFFFFFFFF);
        body.drawString("Font backend: " + render2D.getClass().getSimpleName(),
                8, 26, 0xFF55FFFF);
        body.drawString("ASCII 0123456789", 8, 40, 0xFFAAAAAA);
        body.drawString("中文测试：钻石剑 / 迅捷", 8, 54, 0xFFFFAA00);

        // Large high-contrast marker: if this is invisible too, the problem is
        // GL state/coordinates rather than the glyph atlas.
        FontRenderer marker = render2D.getFont(28.0f, true);
        marker.drawCenteredString("TEST", render2D.getWidth() / 2.0,
                render2D.getHeight() / 2.0 - 24.0, 0xFFFF3030);
    }
}
