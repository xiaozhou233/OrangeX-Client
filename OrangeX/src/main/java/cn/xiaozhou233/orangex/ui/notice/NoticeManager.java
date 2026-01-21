package cn.xiaozhou233.orangex.ui.notice;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NoticeManager {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<Notice> notices = new ArrayList<>();
    private final FontManager fontManager = OrangeX.getFontManager();

    public NoticeManager(EventBus eventBus) {
        eventBus.register(this);
    }

    public void addNotice(String title, String content, long duration, int bgColor) {
        notices.add(new Notice(title, content, duration, bgColor));
    }

    @Subscribe
    public void render(EventRender2D event) {
        if (notices.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer font = mc.fontRendererObj;

        Iterator<Notice> it = notices.iterator();
        while (it.hasNext()) {
            Notice notice = it.next();

            long elapsed = System.currentTimeMillis() - notice.startTime;
            if (elapsed > notice.duration + 800) {
                it.remove();
                continue;
            }

            int titleWidth = font.getStringWidth(notice.title);
            int contentWidth = font.getStringWidth(notice.content);

            int maxWidth = sr.getScaledWidth() / 3;
            int minWidth = 140;

            int width = Math.max(Math.max(titleWidth, contentWidth) + 26, minWidth);
            width = Math.min(width, maxWidth);

            int height = 42;

            int targetX = sr.getScaledWidth() - width - 10;
            int y = sr.getScaledHeight() - height - 10;

            int x;
            if (elapsed < 200) {
                float t = elapsed / 200f;
                x = (int) (sr.getScaledWidth() + (targetX - sr.getScaledWidth()) * t);
            } else if (elapsed > notice.duration) {
                float t = (elapsed - notice.duration) / 600f;
                x = (int) (targetX + (sr.getScaledWidth() - targetX) * t);
            } else {
                x = targetX;
            }


            // ====== 渲染状态 ======
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            int bg = 0xC8000000;
            int border = 0xFFFFFFFF;
            int textColor = 0xFFFFFFFF;

            drawRect(x, y, x + width, y + height, bg);

            drawRect(x, y, x + width, y + 1, border);
            drawRect(x, y + height - 1, x + width, y + height, border);
            drawRect(x, y, x + 1, y + height, border);
            drawRect(x + width - 1, y, x + width, y + height, border);

            fontManager.MontserratRegular18.drawString(notice.title, x + 8, y + 10, textColor);
            fontManager.MontserratRegular18.drawString(notice.content, x + 8, y + 24, textColor);

            GlStateManager.popMatrix();

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
