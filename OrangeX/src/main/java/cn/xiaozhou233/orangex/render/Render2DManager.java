package cn.xiaozhou233.orangex.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontBackendFactory;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import org.greenrobot.eventbus.EventBus;

/**
 * Owns the font cache and turns one platform render callback into an
 * {@link EventRender2D}.
 */
public final class Render2DManager {

    private final FontBackend backend;
    private final FontManager fonts;

    /**
     * Whether the overlay is drawn on screens that are not backed by a loaded
     * level (title screen, world selection, ...). 1.8.9 never renders the HUD
     * there, and this defaults to the same behaviour on every version.
     * Enable with {@code -Dorangex.renderInMenus=true}.
     */
    private volatile boolean renderInMenus = Boolean.getBoolean("orangex.renderInMenus");

    public Render2DManager(RuntimeInfo runtimeInfo,
                           MappingResolver mappingResolver,
                           Object minecraft) {
        this.backend = FontBackendFactory.create(
                runtimeInfo, mappingResolver, minecraft);
        this.fonts = new FontManager(backend);
    }

    public void fire(Object platformContext) {
        fire(platformContext, true, Float.NaN);
    }

    /**
     * @param inWorld {@code true} when a level is loaded (the game's own
     *                "should render level" flag). When {@code false} the
     *                event is dropped unless {@link #isRenderInMenus()} is on.
     */
    public void fire(Object platformContext, boolean inWorld) {
        fire(platformContext, inWorld, Float.NaN);
    }

    /**
     * @param partialTicks the partial tick the game rendered this frame with,
     *                     or {@link Float#NaN} when the caller has none.
     */
    public void fire(Object platformContext, boolean inWorld,
                     float partialTicks) {
        if (!inWorld && !renderInMenus) {
            return;
        }

        EventBus eventBus = OrangeX.getInstance().getEventBus();
        if (!eventBus.hasSubscriberForEvent(EventRender2D.class)) {
            return;
        }

        // Pin atlases used by this frame and release long-idle ones (e.g. the
        // previous GUI scale's faces) before they are needed again.
        fonts.beginFrame();

        Render2D context = backend.createRender2D(fonts, platformContext);
        try {
            eventBus.post(new EventRender2D(context, inWorld, partialTicks));
        } finally {
            context.flush();
        }
    }

    public boolean isRenderInMenus() {
        return renderInMenus;
    }

    public void setRenderInMenus(boolean renderInMenus) {
        this.renderInMenus = renderInMenus;
    }

    public String getBackendName() {
        return backend.getName();
    }

    public FontManager getFonts() {
        return fonts;
    }
}
