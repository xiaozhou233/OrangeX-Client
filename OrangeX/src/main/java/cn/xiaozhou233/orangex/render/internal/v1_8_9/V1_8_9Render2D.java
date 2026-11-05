package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontFace;
import cn.xiaozhou233.orangex.render.font.FontFamily;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.font.TrueTypeFontRenderer;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.8.9 2D context. Coordinates are the vanilla {@code ScaledResolution}
 * pixels, which matches the ortho set up by {@code GuiIngame#renderGameOverlay}.
 */
final class V1_8_9Render2D implements Render2D {

    private static final String SCALED_RESOLUTION =
            "net/minecraft/client/gui/ScaledResolution";
    private static final int ARC_SEGMENTS = 8;

    private final FontManager fonts;
    private final double guiScale;
    private final V1_8_9GlyphDrawer drawer;
    private final V1_8_9RenderState state;
    private final int width;
    private final int height;
    private final List<int[]> scissorStack = new ArrayList<int[]>();
    private final IntBuffer scissorBuffer = BufferUtils.createIntBuffer(16);

    V1_8_9Render2D(FontManager fonts, RuntimeInfo runtimeInfo,
                   MappingResolver resolver, Object minecraft,
                   V1_8_9GlyphDrawer drawer, V1_8_9RenderState state) {
        this.fonts = fonts;
        this.drawer = drawer;
        this.state = state;

        int resolvedWidth = 854;
        int resolvedHeight = 480;
        int resolvedScale = 1;

        try {
            MappingClass mappingClass = resolver.mapClass(SCALED_RESOLUTION);
            Class<?> scaledClass = Class.forName(
                    mappingClass.getTargetName().replace('/', '.'),
                    false, runtimeInfo.getMinecraftClassLoader());
            Constructor<?> constructor = scaledClass.getDeclaredConstructor(
                    minecraft.getClass());
            constructor.setAccessible(true);
            Object scaled = constructor.newInstance(minecraft);

            Method widthMethod = method(scaledClass, resolver, "getScaledWidth", "()I");
            Method heightMethod = method(scaledClass, resolver, "getScaledHeight", "()I");
            Method scaleMethod = method(scaledClass, resolver, "getScaleFactor", "()I");

            resolvedWidth = (Integer) widthMethod.invoke(scaled);
            resolvedHeight = (Integer) heightMethod.invoke(scaled);
            resolvedScale = (Integer) scaleMethod.invoke(scaled);
        } catch (Throwable throwable) {
            System.err.println("[OrangeX] Failed to resolve ScaledResolution for 1.8.9: "
                    + throwable);
        }

        this.width = resolvedWidth;
        this.height = resolvedHeight;
        this.guiScale = Math.max(1, resolvedScale);
    }

    private static Method method(Class<?> owner, MappingResolver resolver,
                                 String name, String descriptor) throws Exception {
        MappingMethod mapping = resolver.mapMethod(
                SCALED_RESOLUTION, name, descriptor);
        Method method = owner.getDeclaredMethod(mapping.getTargetName());
        method.setAccessible(true);
        return method;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public double getGuiScale() {
        return guiScale;
    }

    @Override
    public FontRenderer getFont(float size) {
        return getFont(size, false);
    }

    @Override
    public FontRenderer getFont(float size, boolean bold) {
        FontFace face = fonts.getFace(FontFamily.PROXIMA, size, bold, guiScale);
        return new TrueTypeFontRenderer(fonts, face, bold, guiScale, drawer);
    }

    @Override
    public void fillRect(double x, double y, double width, double height, int argb) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        state.begin();
        state.disableTexture();
        state.setColor(argb);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
    }

    @Override
    public void fillGradient(double x, double y, double width, double height,
                             int topArgb, int bottomArgb) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        state.begin();
        state.disableTexture();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        state.setColor(topArgb);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        state.setColor(bottomArgb);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
    }

    @Override
    public void drawRect(double x, double y, double width, double height,
                         double thickness, int argb) {
        if (thickness <= 0.0 || width <= 0.0 || height <= 0.0) {
            return;
        }
        fillRect(x, y, width, thickness, argb);
        fillRect(x, y + height - thickness, width, thickness, argb);
        fillRect(x, y + thickness, thickness, height - thickness * 2.0, argb);
        fillRect(x + width - thickness, y + thickness,
                thickness, height - thickness * 2.0, argb);
    }

    @Override
    public void fillRoundedRect(double x, double y, double width, double height,
                                double radius, int argb) {
        fillRoundedRectInternal(x, y, width, height, radius, argb);
    }

    @Override
    public void fillRoundedRect(double x, double y, double width, double height,
                                double radius, double borderWidth,
                                int argb, int borderArgb) {
        if (borderWidth <= 0.0) {
            fillRoundedRectInternal(x, y, width, height, radius, argb);
            return;
        }
        fillRoundedRectInternal(x, y, width, height, radius, borderArgb);
        double inner = Math.max(0.0, borderWidth);
        double innerWidth = width - inner * 2.0;
        double innerHeight = height - inner * 2.0;
        if (innerWidth > 0.0 && innerHeight > 0.0) {
            fillRoundedRectInternal(x + inner, y + inner,
                    innerWidth, innerHeight,
                    Math.max(0.0, radius - inner), argb);
        }
    }

    private void fillRoundedRectInternal(double x, double y, double width,
                                         double height, double radius, int argb) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        double r = Math.min(radius, Math.min(width, height) / 2.0);
        if (r <= 0.0) {
            fillRect(x, y, width, height, argb);
            return;
        }

        List<double[]> points = roundedRectPerimeter(x, y, width, height, r);
        if (points.isEmpty()) {
            return;
        }

        state.begin();
        state.disableTexture();
        state.setColor(argb);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x + width / 2.0, y + height / 2.0);
        for (double[] point : points) {
            GL11.glVertex2d(point[0], point[1]);
        }
        double[] first = points.get(0);
        GL11.glVertex2d(first[0], first[1]);
        GL11.glEnd();
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2,
                         double thickness, int argb) {
        if (thickness <= 0.0) {
            return;
        }
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (Math.abs(dy) < 0.01) {
            fillRect(Math.min(x1, x2), y1 - thickness / 2.0,
                    Math.abs(dx), thickness, argb);
            return;
        }
        if (Math.abs(dx) < 0.01) {
            fillRect(x1 - thickness / 2.0, Math.min(y1, y2),
                    thickness, Math.abs(dy), argb);
            return;
        }
        state.begin();
        state.disableTexture();
        state.setColor(argb);
        GL11.glLineWidth((float) thickness);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }

    @Override
    public void enableScissor(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        state.begin();

        scissorBuffer.clear();
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, scissorBuffer);
        scissorStack.add(new int[]{
                scissorBuffer.get(0), scissorBuffer.get(1),
                scissorBuffer.get(2), scissorBuffer.get(3)
        });

        int scale = (int) Math.round(guiScale);
        int sx = (int) Math.round(x * scale);
        int sy = (int) Math.round((this.height - y - height) * scale);
        int sw = (int) Math.round(width * scale);
        int sh = (int) Math.round(height * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(sx, sy, Math.max(0, sw), Math.max(0, sh));
    }

    @Override
    public void disableScissor() {
        if (scissorStack.isEmpty()) {
            return;
        }
        int[] box = scissorStack.remove(scissorStack.size() - 1);
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            GL11.glScissor(box[0], box[1], box[2], box[3]);
        }
    }

    private static List<double[]> roundedRectPerimeter(double x, double y,
                                                       double width, double height,
                                                       double radius) {
        List<double[]> points = new ArrayList<double[]>();
        double right = x + width;
        double bottom = y + height;

        addArc(points, x + radius, y + radius, radius, 180.0, 270.0);
        addArc(points, right - radius, y + radius, radius, 270.0, 360.0);
        addArc(points, right - radius, bottom - radius, radius, 0.0, 90.0);
        addArc(points, x + radius, bottom - radius, radius, 90.0, 180.0);
        return points;
    }

    private static void addArc(List<double[]> points, double centerX,
                               double centerY, double radius,
                               double startDegrees, double endDegrees) {
        for (int i = 0; i <= ARC_SEGMENTS; i++) {
            double angle = Math.toRadians(startDegrees
                    + (endDegrees - startDegrees) * i / ARC_SEGMENTS);
            points.add(new double[]{
                    centerX + Math.cos(angle) * radius,
                    centerY + Math.sin(angle) * radius
            });
        }
    }

    @Override
    public void flush() {
        state.end();
        drawer.reset();
        scissorStack.clear();
    }
}
