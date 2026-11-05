package cn.xiaozhou233.orangex.render.internal;

/**
 * Opaque GPU texture owned by one {@link cn.xiaozhou233.orangex.render.font.FontAtlas}.
 * The concrete type depends on the Minecraft version.
 */
public interface FontTexture {

    int getWidth();

    int getHeight();

    void close();
}
