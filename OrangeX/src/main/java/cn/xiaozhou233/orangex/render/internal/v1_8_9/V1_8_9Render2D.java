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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 1.8.9 2D context. Coordinates are the vanilla {@code ScaledResolution}
 * pixels, which matches the ortho set up by {@code GuiIngame#renderGameOverlay}.
 */
final class V1_8_9Render2D implements Render2D {

    private static final String SCALED_RESOLUTION =
            "net/minecraft/client/gui/ScaledResolution";

    private final FontManager fonts;
    private final double guiScale;
    private final V1_8_9GlyphDrawer drawer;
    private final int width;
    private final int height;

    V1_8_9Render2D(FontManager fonts, RuntimeInfo runtimeInfo,
                   MappingResolver resolver, Object minecraft,
                   V1_8_9GlyphDrawer drawer) {
        this.fonts = fonts;
        this.drawer = drawer;

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
    public void flush() {
        drawer.flush();
    }
}
