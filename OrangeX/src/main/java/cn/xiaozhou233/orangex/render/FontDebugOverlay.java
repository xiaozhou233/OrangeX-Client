package cn.xiaozhou233.orangex.render;

import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.input.ClientInput;
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

        render2D.fillRoundedRect(4, 4, 250, 120, 8,
                1.0, 0xD0141414, 0xFF303030);
        render2D.fillGradient(10, 112, 238, 6,
                0xFF55FFFF, 0xFF55FF55);
        render2D.drawLine(10, 124, 248, 124, 1.0, 0x80FFFFFF);

        FontRenderer title = render2D.getFont(14.0f, true);
        FontRenderer body = render2D.getFont(10.0f, false);

        title.drawStringWithShadow("OrangeX", 8, 8, 0xFFFFFFFF);
        body.drawString("Font backend: " + render2D.getClass().getSimpleName(),
                8, 26, 0xFF55FFFF);
        body.drawString("ASCII 0123456789", 8, 40, 0xFFAAAAAA);
        body.drawString("中文测试：钻石剑 / 迅捷", 8, 54, 0xFFFFAA00);
        body.drawString("Input: " + (ClientInput.isOpen() ? "OPEN" : "closed")
                        + "  mouse " + (int) ClientInput.getMouseX()
                        + ", " + (int) ClientInput.getMouseY(),
                8, 68, 0xFF55FF55);
        body.drawString("M: toggle input screen", 8, 82, 0xFF888888);

        // Large high-contrast marker: if this is invisible too, the problem is
        // GL state/coordinates rather than the glyph atlas.
        FontRenderer marker = render2D.getFont(28.0f, true);
        marker.drawCenteredString("TEST", render2D.getWidth() / 2.0,
                render2D.getHeight() / 2.0 - 24.0, 0xFFFF3030);

        if (ClientInput.isOpen()) {
            double mouseX = ClientInput.getMouseX();
            double mouseY = ClientInput.getMouseY();
            render2D.drawLine(mouseX - 7, mouseY, mouseX + 7, mouseY, 1.0, 0xFFFF4040);
            render2D.drawLine(mouseX, mouseY - 7, mouseX, mouseY + 7, 1.0, 0xFFFF4040);
        }
    }
}
