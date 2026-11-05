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
    private final int width;
    private final int height;
    private final double guiScale;

    V26_2Render2D(FontManager fonts, Object gui, ClassLoader loader) {
        this.fonts = fonts;

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
    public void flush() {
        // Everything was submitted to the GuiRenderState; nothing to restore.
    }
}
