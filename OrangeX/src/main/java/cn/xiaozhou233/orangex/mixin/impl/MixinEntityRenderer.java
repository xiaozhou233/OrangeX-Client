package cn.xiaozhou233.orangex.mixin.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender3D;
import cn.yapeteam.ymixin.annotations.Inject;
import cn.yapeteam.ymixin.annotations.Local;
import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.annotations.Target;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(
            method = "renderWorldPass", desc = "(IFJ)V",
            target = @Target(
                    value = "INVOKESTATIC",
                    target = "net/minecraft/client/renderer/GlStateManager.disableFog()V",
                    shift = Target.Shift.AFTER
            )
    )
    private void render(@Local(source = "partialTicks", index = 2) float partialTicks) {
        OrangeX.getInstance().getEventBus().post(new EventRender3D(partialTicks));
    }
}