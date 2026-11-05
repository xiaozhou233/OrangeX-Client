package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontFace;
import cn.xiaozhou233.orangex.render.font.FontFamily;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.font.TrueTypeFontRenderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 26.x 2D context.
 *
 * <p>26.x no longer exposes {@code GuiGraphics}; the GUI is collected into a
 * {@code GuiRenderState} by {@code Gui#extractRenderState}. The render hook
 * passes that {@code Gui} instance in, and this class creates a private
 * {@code GuiGraphicsExtractor} over the same render state to queue glyph
 * blits.</p>
 */
final class V26_2Render2D implements Render2D {

    private final FontManager fonts;
    private final V26_2GlyphDrawer drawer;
    private final Object extractor;
    private final Method innerBlit;
    private final Object guiPipeline;
    private final Method fillMethod;
    private final Method fillGradientMethod;
    private final Method enableScissorMethod;
    private final Method disableScissorMethod;
    private final V26_2ShapeCache shapes;
    private final int width;
    private final int height;
    private final double guiScale;

    V26_2Render2D(FontManager fonts, Object gui, ClassLoader loader,
                  V26_2ShapeCache shapes) {
        this.fonts = fonts;
        this.shapes = shapes;

        try {
            Field minecraftField = field(gui.getClass(), "minecraft");
            Object minecraft = minecraftField.get(gui);

            Field renderStateField = field(gui.getClass(), "guiRenderState");
            Object guiRenderState = renderStateField.get(gui);

            Class<?> renderStateClass = Class.forName(
                    "net.minecraft.client.renderer.state.gui.GuiRenderState",
                    false, loader);
            Class<?> extractorClass = Class.forName(
                    "net.minecraft.client.gui.GuiGraphicsExtractor",
                    false, loader);

            Constructor<?> extractorConstructor = extractorClass.getDeclaredConstructor(
                    minecraft.getClass(), renderStateClass, int.class, int.class);
            extractorConstructor.setAccessible(true);
            Object extractor = extractorConstructor.newInstance(
                    minecraft, guiRenderState, 0, 0);

            Object window = minecraft.getClass().getMethod("getWindow").invoke(minecraft);
            this.width = (Integer) window.getClass().getMethod("getGuiScaledWidth")
                    .invoke(window);
            this.height = (Integer) window.getClass().getMethod("getGuiScaledHeight")
                    .invoke(window);
            this.guiScale = Math.max(1, (Integer) window.getClass()
                    .getMethod("getGuiScale").invoke(window));

            Class<?> pipelineClass = Class.forName(
                    "com.mojang.blaze3d.pipeline.RenderPipeline", false, loader);
            Class<?> textureViewClass = Class.forName(
                    "com.mojang.blaze3d.textures.GpuTextureView", false, loader);
            Class<?> samplerClass = Class.forName(
                    "com.mojang.blaze3d.textures.GpuSampler", false, loader);

            Object guiPipeline = Class.forName(
                            "net.minecraft.client.renderer.RenderPipelines", false, loader)
                    .getField("GUI_TEXTURED").get(null);

            Method innerBlit = extractorClass.getDeclaredMethod("innerBlit",
                    pipelineClass, textureViewClass, samplerClass,
                    int.class, int.class, int.class, int.class,
                    float.class, float.class, float.class, float.class,
                    int.class);
            innerBlit.setAccessible(true);

            this.extractor = extractor;
            this.innerBlit = innerBlit;
            this.guiPipeline = guiPipeline;
            this.fillMethod = extractorClass.getMethod("fill",
                    int.class, int.class, int.class, int.class, int.class);
            this.fillGradientMethod = extractorClass.getMethod("fillGradient",
                    int.class, int.class, int.class, int.class,
                    int.class, int.class);
            this.enableScissorMethod = extractorClass.getMethod("enableScissor",
                    int.class, int.class, int.class, int.class);
            this.disableScissorMethod = extractorClass.getMethod("disableScissor");
            this.drawer = new V26_2GlyphDrawer(extractor, innerBlit, guiPipeline);
        } catch (Throwable throwable) {
            throw new IllegalStateException(
                    "Cannot initialise the 26.2 GUI font context", throwable);
        }
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Continue with the superclass.
            }
        }
        throw new NoSuchFieldException(name + " on " + owner.getName());
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
        int x0 = (int) Math.round(x);
        int y0 = (int) Math.round(y);
        int x1 = (int) Math.round(x + width);
        int y1 = (int) Math.round(y + height);
        invokeFill(x0, y0, x1, y1, argb);
    }

    @Override
    public void fillGradient(double x, double y, double width, double height,
                             int topArgb, int bottomArgb) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        try {
            fillGradientMethod.invoke(extractor,
                    (int) Math.round(x), (int) Math.round(y),
                    (int) Math.round(x + width), (int) Math.round(y + height),
                    topArgb, bottomArgb);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to draw a GUI gradient", throwable);
        }
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
        double inset = Math.max(0.0, borderWidth);
        double innerWidth = width - inset * 2.0;
        double innerHeight = height - inset * 2.0;
        if (innerWidth > 0.0 && innerHeight > 0.0) {
            fillRoundedRectInternal(x + inset, y + inset,
                    innerWidth, innerHeight,
                    Math.max(0.0, radius - inset), argb);
        }
    }

    private void fillRoundedRectInternal(double x, double y, double width,
                                         double height, double radius, int argb) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        double clamped = Math.min(radius, Math.min(width, height) / 2.0);
        if (clamped <= 0.5) {
            fillRect(x, y, width, height, argb);
            return;
        }

        int x0 = (int) Math.round(x);
        int y0 = (int) Math.round(y);
        int x1 = (int) Math.round(x + width);
        int y1 = (int) Math.round(y + height);
        int corner = Math.max(1, (int) Math.round(clamped));
        corner = Math.min(corner, Math.min((x1 - x0) / 2, (y1 - y0) / 2));
        if (corner <= 0) {
            fillRect(x, y, width, height, argb);
            return;
        }

        // Straight strips.
        invokeFill(x0 + corner, y0, x1 - corner, y0 + corner, argb);
        invokeFill(x0 + corner, y1 - corner, x1 - corner, y1, argb);
        invokeFill(x0 + corner, y0 + corner, x1 - corner, y1 - corner, argb);
        invokeFill(x0, y0 + corner, x0 + corner, y1 - corner, argb);
        invokeFill(x1 - corner, y0 + corner, x1, y1 - corner, argb);

        int radiusPixels = Math.max(2, (int) Math.round(clamped * guiScale));
        V26_2GpuTexture texture = shapes.corner(radiusPixels);

        // Antialiased quarter-circle corners. Flipping U/V mirrors the same
        // texture for the other three corners.
        invokeBlit(texture, x0, y0, x0 + corner, y0 + corner,
                0.0f, 0.0f, 1.0f, 1.0f, argb);
        invokeBlit(texture, x1 - corner, y0, x1, y0 + corner,
                1.0f, 0.0f, 0.0f, 1.0f, argb);
        invokeBlit(texture, x0, y1 - corner, x0 + corner, y1,
                0.0f, 1.0f, 1.0f, 0.0f, argb);
        invokeBlit(texture, x1 - corner, y1 - corner, x1, y1,
                1.0f, 1.0f, 0.0f, 0.0f, argb);
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

        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0 : (double) i / (double) steps;
            fillRect(x1 + dx * t - thickness / 2.0,
                    y1 + dy * t - thickness / 2.0,
                    thickness, thickness, argb);
        }
    }

    @Override
    public void enableScissor(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        try {
            enableScissorMethod.invoke(extractor,
                    (int) Math.round(x), (int) Math.round(y),
                    (int) Math.round(x + width), (int) Math.round(y + height));
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to enable a GUI scissor", throwable);
        }
    }

    @Override
    public void disableScissor() {
        try {
            disableScissorMethod.invoke(extractor);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to disable a GUI scissor", throwable);
        }
    }

    private void invokeFill(int x0, int y0, int x1, int y1, int argb) {
        try {
            fillMethod.invoke(extractor, x0, y0, x1, y1, argb);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to draw a GUI rectangle", throwable);
        }
    }

    private void invokeBlit(V26_2GpuTexture texture, int x0, int y0, int x1, int y1,
                            float u0, float v0, float u1, float v1, int argb) {
        try {
            innerBlit.invoke(extractor, guiPipeline,
                    texture.textureView, texture.sampler,
                    x0, y0, x1, y1, u0, u1, v0, v1, argb);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to draw a GUI corner", throwable);
        }
    }

    @Override
    public void flush() {
        // Everything was submitted to the GuiRenderState; nothing to restore.
    }
}
