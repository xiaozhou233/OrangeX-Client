package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.event.impl.EventRender3D;
import cn.xiaozhou233.orangex.font.SmoothFontRenderer;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.option.impl.BooleanOption;
import cn.xiaozhou233.orangex.module.option.impl.ColorOption;
import cn.xiaozhou233.orangex.module.option.impl.IntOption;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class NameTag extends Module {
    private SmoothFontRenderer font;
    private final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
    private final List<NametagEntry> renderQueue = new ArrayList<>();

    public NameTag() {
        super("NameTag", "Render name tag", ModuleCategory.RENDER);
        addOption(new BooleanOption("Render Players", "Render players", this, true));
        addOption(new BooleanOption("Render Self", "Render self", this, false));
        addOption(new BooleanOption("Show Health", "Show health", this, true));
        addOption(new BooleanOption("Background", "Show background", this, true));
        addOption(new IntOption("Font Size", "Font size", this, 18, 12, 30, 2));
        addOption(new ColorOption("Text Color", "Text color", this, 0xFFFFFFFF));
        addOption(new ColorOption("Background Color", "Background color", this, 0x80000000));
    }

    @Subscribe
    public void onRender3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        renderQueue.clear();

        BooleanOption renderPlayers = getOption("Render Players", BooleanOption.class);
        BooleanOption renderSelf = getOption("Render Self", BooleanOption.class);
        BooleanOption showHealth = getOption("Show Health", BooleanOption.class);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) obj;

            if (player == mc.thePlayer && !renderSelf.getValue()) continue;
            if (player != mc.thePlayer && !renderPlayers.getValue()) continue;
            if (player.isDead) continue;

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

            float height = player.height + 0.5f;

            project(
                    (float) (x - mc.getRenderManager().viewerPosX),
                    (float) (y - mc.getRenderManager().viewerPosY + height),
                    (float) (z - mc.getRenderManager().viewerPosZ),
                    modelview, projection, viewport, screenCoords
            );

            float screenZ = screenCoords.get(2);
            if (screenZ >= 1.0f) continue;

            int scaleFactor = new ScaledResolution(mc).getScaleFactor();
            float screenX = screenCoords.get(0) / scaleFactor;
            float screenY = (mc.displayHeight - screenCoords.get(1)) / scaleFactor;

            String name = player.getDisplayName().getFormattedText();
            String healthText = "";

            if (showHealth.getValue()) {
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                String healthColor = health > maxHealth * 0.5f ? "\u00a7a" :
                        health > maxHealth * 0.25f ? "\u00a7e" : "\u00a7c";
                healthText = " " + healthColor + "[" + (int) Math.ceil(health) + "/" + (int) Math.ceil(maxHealth) + "]";
            }

            String fullText = name + healthText;
            renderQueue.add(new NametagEntry(fullText, screenX, screenY));
        }
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        if (renderQueue.isEmpty()) return;

        IntOption fontSize = getOption("Font Size", IntOption.class);
        int size = fontSize.getValue();
        if (font == null || font.getFontSize() != size) {
            font = OrangeX.getInstance().getStbFontManager().getProxima(size);
        }

        BooleanOption showBackground = getOption("Background", BooleanOption.class);
        ColorOption textColor = getOption("Text Color", ColorOption.class);
        ColorOption bgColor = getOption("Background Color", ColorOption.class);

        for (NametagEntry entry : renderQueue) {
            String fullText = entry.text;
            double textWidth = font.getStringWidth(fullText);
            double textHeight = font.getStringHeight(fullText);

            double renderX = entry.screenX - textWidth / 2;
            double renderY = entry.screenY - textHeight;

            if (showBackground.getValue()) {
                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                int bg = bgColor.getValue();
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glColor4f(
                        (float) (bg >> 16 & 0xFF) / 255.0f,
                        (float) (bg >> 8 & 0xFF) / 255.0f,
                        (float) (bg & 0xFF) / 255.0f,
                        (float) (bg >> 24 & 0xFF) / 255.0f
                );
                double bw = textWidth / 2 + 3;
                double bh = textHeight + 1;
                GL11.glVertex2d(entry.screenX - bw, renderY - 1);
                GL11.glVertex2d(entry.screenX - bw, renderY + bh);
                GL11.glVertex2d(entry.screenX + bw, renderY + bh);
                GL11.glVertex2d(entry.screenX + bw, renderY - 1);
                GL11.glEnd();
                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
            }

            font.renderString(fullText, renderX + 0.5, renderY + 0.5, 0x80000000, false);
            font.renderString(fullText, renderX, renderY, textColor.getValue(), false);
        }

        renderQueue.clear();
    }

    private static void project(float x, float y, float z,
                                  FloatBuffer modelview, FloatBuffer projection,
                                  IntBuffer viewport, FloatBuffer screenCoords) {
        float[] v = new float[4];
        float[] c = new float[4];

        v[0] = modelview.get(0) * x + modelview.get(4) * y + modelview.get(8) * z + modelview.get(12);
        v[1] = modelview.get(1) * x + modelview.get(5) * y + modelview.get(9) * z + modelview.get(13);
        v[2] = modelview.get(2) * x + modelview.get(6) * y + modelview.get(10) * z + modelview.get(14);
        v[3] = modelview.get(3) * x + modelview.get(7) * y + modelview.get(11) * z + modelview.get(15);

        c[0] = projection.get(0) * v[0] + projection.get(4) * v[1] + projection.get(8) * v[2] + projection.get(12) * v[3];
        c[1] = projection.get(1) * v[0] + projection.get(5) * v[1] + projection.get(9) * v[2] + projection.get(13) * v[3];
        c[2] = projection.get(2) * v[0] + projection.get(6) * v[1] + projection.get(10) * v[2] + projection.get(14) * v[3];
        c[3] = projection.get(3) * v[0] + projection.get(7) * v[1] + projection.get(11) * v[2] + projection.get(15) * v[3];

        if (c[3] == 0.0f) return;

        float ndcX = c[0] / c[3];
        float ndcY = c[1] / c[3];
        float ndcZ = c[2] / c[3];

        screenCoords.put(0, viewport.get(0) + viewport.get(2) * (ndcX + 1.0f) / 2.0f);
        screenCoords.put(1, viewport.get(1) + viewport.get(3) * (ndcY + 1.0f) / 2.0f);
        screenCoords.put(2, (ndcZ + 1.0f) / 2.0f);
    }

    private static class NametagEntry {
        final String text;
        final float screenX;
        final float screenY;

        NametagEntry(String text, float screenX, float screenY) {
            this.text = text;
            this.screenX = screenX;
            this.screenY = screenY;
        }
    }
}