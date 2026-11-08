package cn.xiaozhou233.orangex.modules.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.value.BooleanValue;
import cn.xiaozhou233.orangex.value.ModeValue;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
import cn.xiaozhou233.orangex.wrapper.common.EntityTarget;
import cn.xiaozhou233.orangex.wrapper.common.ScreenPoint;
import cn.xiaozhou233.orangex.wrapper.common.Vec3;
import cn.xiaozhou233.orangex.wrapper.common.WorldSnapshot;
import org.greenrobot.eventbus.Subscribe;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

/**
 * 2D ESP: projects players to the screen and draws a box, name, distance and
 * health bar around each one.
 *
 * <p>All drawing happens in GUI pixel space through {@link Render2D}, so it
 * works with both the 1.8.9 and the modern font backends. World data comes
 * from the cross-version {@link WorldView} wrapper, never from game classes
 * directly.</p>
 */
public class ESP extends Module {

    private static final float NAME_FONT_SIZE = 9.0F;

    private static final double NAME_GAP = 2.0D;
    private static final double BOX_PADDING = 0.5D;
    private static final double CORNER_LENGTH = 4.0D;
    private static final double HEALTH_BAR_WIDTH = 2.0D;
    private static final double HEALTH_BAR_GAP = 2.0D;

    private static final double MIN_BOX_HEIGHT = 2.0D;
    private static final double MIN_BOX_WIDTH = 2.0D;

    private static final int ORANGE = 0xFFFF8A00;
    private static final int FILL_ALPHA = 32;
    private static final int HEALTH_BACKGROUND = 0x80000000;

    private final BooleanValue box = addBoolean("Box", true);
    private final BooleanValue fill = addBoolean("Fill", true);
    private final BooleanValue health = addBoolean("Health", true);
    private final BooleanValue name = addBoolean("Name", true);
    private final BooleanValue distance = addBoolean("Distance", true);
    private final ModeValue style = addMode("Style", "Box", "Box", "Corners");
    private final ModeValue colorMode = addMode("Color", "Orange", "Orange", "Health", "Rainbow");

    public ESP() {
        super("ESP", ModuleCategory.RENDER);
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        if (!event.isInWorld()) {
            return;
        }

        WorldView world = OrangeX.getInstance().getWorldView();
        if (world == null) {
            return;
        }

        WorldSnapshot snapshot;
        try {
            snapshot = world.snapshot();
        } catch (Throwable throwable) {
            // A broken wrapper must not kill the whole HUD frame.
            System.err.println("[ESP] Failed to read world snapshot: "
                    + throwable.getMessage());
            return;
        }

        if (snapshot == null) {
            return;
        }

        Render2D graphics = event.getRender2D();
        int screenWidth = graphics.getWidth();
        int screenHeight = graphics.getHeight();

        FontRenderer font = graphics.getFont(NAME_FONT_SIZE);
        double fontHeight = font.getStringHeight();

        List<EntityTarget> targets = snapshot.getTargets();
        for (int index = 0; index < targets.size(); index++) {
            EntityTarget target = targets.get(index);
            renderTarget(graphics, font, fontHeight, snapshot, target, index,
                    screenWidth, screenHeight);
        }
    }

    private void renderTarget(Render2D graphics,
                              FontRenderer font,
                              double fontHeight,
                              WorldSnapshot snapshot,
                              EntityTarget target,
                              int index,
                              int screenWidth,
                              int screenHeight) {
        if (target.getHeight() <= 0.0D) {
            return;
        }

        Vec3 feet = target.getPosition();
        Vec3 head = new Vec3(feet.getX(), feet.getY() + target.getHeight(), feet.getZ());

        ScreenPoint feetPoint = snapshot.project(feet, screenWidth, screenHeight);
        ScreenPoint headPoint = snapshot.project(head, screenWidth, screenHeight);

        if (!feetPoint.isVisible() || !headPoint.isVisible()) {
            return;
        }

        double boxHeight = feetPoint.getY() - headPoint.getY();
        if (boxHeight < MIN_BOX_HEIGHT) {
            return;
        }

        double boxWidth = Math.max(MIN_BOX_WIDTH,
                boxHeight * (target.getWidth() / target.getHeight()));
        double centerX = (feetPoint.getX() + headPoint.getX()) * 0.5D;
        double left = centerX - boxWidth * 0.5D;
        double top = headPoint.getY();

        int color = color(target, index);

        if (fill.isEnabled()) {
            graphics.fillRect(left, top, boxWidth, boxHeight,
                    (color & 0x00FFFFFF) | (FILL_ALPHA << 24));
        }

        if (box.isEnabled()) {
            if (style.is("Corners")) {
                drawCorners(graphics, left, top, boxWidth, boxHeight, color);
            } else {
                graphics.drawRect(left - BOX_PADDING, top - BOX_PADDING,
                        boxWidth + BOX_PADDING * 2.0D,
                        boxHeight + BOX_PADDING * 2.0D,
                        1.0D, color);
            }
        }

        if (health.isEnabled()) {
            drawHealthBar(graphics, target, left - HEALTH_BAR_GAP, top, boxHeight);
        }

        String label = label(target);
        if (label == null) {
            return;
        }

        double textWidth = font.getStringWidth(label);
        double textY = top - fontHeight - NAME_GAP;
        font.drawStringWithShadow(label, centerX - textWidth * 0.5D, textY, color);
    }

    private void drawCorners(Render2D graphics, double left, double top,
                             double width, double height, int color) {
        double length = Math.min(CORNER_LENGTH, Math.min(width, height) * 0.5D);
        if (length <= 0.0D) {
            return;
        }

        double right = left + width;
        double bottom = top + height;

        // Top-left
        graphics.fillRect(left, top, length, 1.0D, color);
        graphics.fillRect(left, top, 1.0D, length, color);

        // Top-right
        graphics.fillRect(right - length, top, length, 1.0D, color);
        graphics.fillRect(right - 1.0D, top, 1.0D, length, color);

        // Bottom-left
        graphics.fillRect(left, bottom - 1.0D, length, 1.0D, color);
        graphics.fillRect(left, bottom - length, 1.0D, length, color);

        // Bottom-right
        graphics.fillRect(right - length, bottom - 1.0D, length, 1.0D, color);
        graphics.fillRect(right - 1.0D, bottom - length, 1.0D, length, color);
    }

    private void drawHealthBar(Render2D graphics, EntityTarget target,
                               double x, double top, double height) {
        if (target.getMaxHealth() <= 0.0F) {
            return;
        }

        float fraction = target.getHealth() / target.getMaxHealth();
        fraction = Math.max(0.0F, Math.min(1.0F, fraction));

        double filled = height * fraction;

        graphics.fillRect(x, top, HEALTH_BAR_WIDTH, height, HEALTH_BACKGROUND);
        graphics.fillRect(x, top + height - filled, HEALTH_BAR_WIDTH, filled,
                healthColor(fraction));
    }

    private String label(EntityTarget target) {
        String nameText = name.isEnabled() ? target.getName() : null;
        String distanceText = distance.isEnabled()
                ? String.format(Locale.ROOT, "%.1fm", target.getDistance())
                : null;

        if (nameText == null) {
            return distanceText;
        }
        if (distanceText == null) {
            return nameText;
        }
        return nameText + "  " + distanceText;
    }

    private int color(EntityTarget target, int index) {
        if (colorMode.is("Health")) {
            float fraction = target.getMaxHealth() <= 0.0F
                    ? 1.0F
                    : target.getHealth() / target.getMaxHealth();
            return healthColor(Math.max(0.0F, Math.min(1.0F, fraction)));
        }

        if (colorMode.is("Rainbow")) {
            float hue = ((System.currentTimeMillis() % 4000L) / 4000.0F)
                    + index * 0.05F;
            return Color.HSBtoRGB(hue - (float) Math.floor(hue), 0.85F, 1.0F);
        }

        return ORANGE;
    }

    private int healthColor(float fraction) {
        return Color.HSBtoRGB(0.33F * fraction, 0.85F, 1.0F);
    }
}
