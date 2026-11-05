package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontTexture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and caches {@link FontFace} instances.
 *
 * <p>One face exists per (family, bold, logical size, GUI scale) tuple. The
 * atlas is rasterised at {@code size * guiScale} pixels so text stays crisp on
 * high GUI scales, while {@link FontFace#getDrawScale()} converts atlas pixels
 * back to GUI pixels.</p>
 */
public final class FontManager {

    private static final int MAX_RASTER_HEIGHT = 64;
    private static final int MAX_CJK_RASTER_HEIGHT = 36;

    private final FontBackend backend;
    private final Map<Key, FontFace> faces = new HashMap<Key, FontFace>();
    private final Map<String, byte[]> fontData = new HashMap<String, byte[]>();

    public FontManager(FontBackend backend) {
        this.backend = backend;
    }

    public synchronized FontFace getFace(FontFamily family, float logicalSize,
                                         boolean bold, double guiScale) {
        if (family == null) {
            family = FontFamily.PROXIMA;
        }
        if (logicalSize <= 0.0f) {
            throw new IllegalArgumentException("logicalSize must be positive");
        }

        int rasterHeight = (int) Math.round(logicalSize * Math.max(1.0, guiScale));
        int maxRasterHeight = family.isCjkCapable()
                ? MAX_CJK_RASTER_HEIGHT
                : MAX_RASTER_HEIGHT;
        if (rasterHeight < 1) {
            rasterHeight = 1;
        } else if (rasterHeight > maxRasterHeight) {
            rasterHeight = maxRasterHeight;
        }

        Key key = new Key(family, bold, rasterHeight, logicalSize);
        FontFace cached = faces.get(key);
        if (cached != null) {
            return cached;
        }

        byte[] data = loadFont(family.getResource(bold));
        long start = System.currentTimeMillis();
        FontAtlas atlas = StbFontBaker.bake(
                data,
                rasterHeight,
                family.isCjkCapable(),
                !"false".equalsIgnoreCase(
                        System.getProperty("orangex.font.fullGbk", "true"))
        );
        FontTexture texture = backend.createTexture(atlas);
        atlas.releasePixels();
        float drawScale = logicalSize / (float) rasterHeight;
        FontFace face = new FontFace(family, atlas, texture, logicalSize, drawScale);
        faces.put(key, face);

        System.out.println("[OrangeX] Baked " + family.name().toLowerCase()
                + (bold ? "-bold" : "") + " " + rasterHeight + "px ("
                + atlas.getGlyphs().size() + " glyphs, "
                + (System.currentTimeMillis() - start) + " ms)");
        return face;
    }

    private byte[] loadFont(String resource) {
        byte[] cached = fontData.get(resource);
        if (cached != null) {
            return cached;
        }

        InputStream stream = FontManager.class.getClassLoader()
                .getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("Missing font resource: " + resource);
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream(64 * 1024);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            byte[] data = output.toByteArray();
            fontData.put(resource, data);
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read font resource: " + resource, e);
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
                // Ignore close failures for a classpath resource.
            }
        }
    }

    public String getBackendName() {
        return backend.getName();
    }

    public void clear() {
        for (FontFace face : faces.values()) {
            face.getTexture().close();
        }
        faces.clear();
    }

    private static final class Key {
        private final FontFamily family;
        private final boolean bold;
        private final int rasterHeight;
        private final float logicalSize;

        private Key(FontFamily family, boolean bold, int rasterHeight,
                    float logicalSize) {
            this.family = family;
            this.bold = bold;
            this.rasterHeight = rasterHeight;
            this.logicalSize = logicalSize;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key)) {
                return false;
            }
            Key key = (Key) other;
            return bold == key.bold
                    && rasterHeight == key.rasterHeight
                    && Float.compare(logicalSize, key.logicalSize) == 0
                    && family == key.family;
        }

        @Override
        public int hashCode() {
            int result = family.hashCode();
            result = 31 * result + (bold ? 1 : 0);
            result = 31 * result + rasterHeight;
            result = 31 * result + Float.floatToIntBits(logicalSize);
            return result;
        }
    }
}
