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

    public Render2DManager(RuntimeInfo runtimeInfo,
                           MappingResolver mappingResolver,
                           Object minecraft) {
        this.backend = FontBackendFactory.create(
                runtimeInfo, mappingResolver, minecraft);
        this.fonts = new FontManager(backend);
    }

    public void fire(Object platformContext) {
        EventBus eventBus = OrangeX.getInstance().getEventBus();
        if (!eventBus.hasSubscriberForEvent(EventRender2D.class)) {
            return;
        }

        Render2D context = backend.createRender2D(fonts, platformContext);
        try {
            eventBus.post(new EventRender2D(context));
        } finally {
            context.flush();
        }
    }

    public String getBackendName() {
        return backend.getName();
    }

    public FontManager getFonts() {
        return fonts;
    }
}
