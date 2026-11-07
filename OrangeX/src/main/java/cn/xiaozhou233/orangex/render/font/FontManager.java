package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontTexture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Creates and caches {@link FontFace} instances.
 *
 * <p>Atlases are cached per (font resource, bake height) and shared between
 * logical sizes; a face is a lightweight view adding the draw scale. The
 * atlas is rasterised at {@code size * guiScale} pixels (rounded up to the
 * family's bake-height bucket) so text stays crisp on high GUI scales, while
 * {@link FontFace#getDrawScale()} converts atlas pixels back to GUI
 * pixels.</p>
 *
 * <p>Textures are bounded by an idle timeout and a byte budget; least recently
 * used atlases are closed when either is exceeded, so changing GUI scale or
 * using many sizes no longer leaks VRAM. Frames should call
 * {@link #beginFrame()} so atlases used by the current frame are pinned.</p>
 */
public final class FontManager {

    private static final int MAX_RASTER_HEIGHT = 64;
    private static final int MAX_CJK_RASTER_HEIGHT = 36;

    /**
     * Quantised bake heights for CJK faces. A full Noto atlas is large, so
     * neighbouring sizes share a bucket instead of each baking their own sheet.
     */
    private static final int[] CJK_BAKE_HEIGHTS =
            new int[]{12, 16, 20, 24, 28, 32, 36};

    private static final long TRIM_INTERVAL_NANOS = 2_000_000_000L;
    private static final long DEFAULT_ATLAS_BUDGET_MB = 128L;
    private static final int DEFAULT_IDLE_SECONDS = 30;

    private static final FontFamily FALLBACK_FAMILY = FontFamily.PROXIMA;

    private final FontBackend backend;
    private final Map<AtlasKey, FontAtlasEntry> atlases =
            new HashMap<AtlasKey, FontAtlasEntry>();
    private final Map<FaceKey, FontFace> faces = new HashMap<FaceKey, FontFace>();
    private final Map<String, byte[]> fontData = new HashMap<String, byte[]>();

    private final boolean fullGbk;
    private final boolean debugCache;
    private final int bytesPerTexel;
    private final long atlasBudgetBytes;
    private final long idleNanos;

    private FontFamily defaultFamily;
    private long frameStartNanos;
    private long lastTrimNanos;

    public FontManager(FontBackend backend) {
        this.backend = backend;
        this.defaultFamily = resolveDefaultFamily();
        this.fullGbk = !"false".equalsIgnoreCase(
                System.getProperty("orangex.font.fullGbk", "true"));
        this.debugCache = Boolean.getBoolean("orangex.font.debugCache");
        this.bytesPerTexel = Math.max(1, backend.getBytesPerTexel());
        this.atlasBudgetBytes = Math.max(8L,
                Long.getLong("orangex.font.atlasBudgetMb", DEFAULT_ATLAS_BUDGET_MB))
                * 1024L * 1024L;
        this.idleNanos = Math.max(1,
                Integer.getInteger("orangex.font.idleSeconds", DEFAULT_IDLE_SECONDS))
                * 1_000_000_000L;
        this.frameStartNanos = System.nanoTime();
        this.lastTrimNanos = this.frameStartNanos;
    }

    private static FontFamily resolveDefaultFamily() {
        String configured = System.getProperty("orangex.font.family");
        if (configured == null || configured.trim().isEmpty()) {
            return FALLBACK_FAMILY;
        }
        FontFamily family = FontFamily.byName(configured);
        if (family == null) {
            System.out.println("[OrangeX] Unknown font family '" + configured
                    + "', falling back to " + FALLBACK_FAMILY.getDisplayName());
            return FALLBACK_FAMILY;
        }
        return family;
    }

    /**
     * The family used by {@code Render2D#getFont(size)}. Defaults to
     * {@link #FALLBACK_FAMILY}, overridable with
     * {@code -Dorangex.font.family=<name>}.
     */
    public synchronized FontFamily getDefaultFamily() {
        return defaultFamily;
    }

    /**
     * Switches the family used by the size-only {@code getFont} overloads.
     * Already created {@link FontFace} instances stay valid.
     */
    public synchronized void setDefaultFamily(FontFamily defaultFamily) {
        this.defaultFamily = defaultFamily != null ? defaultFamily : FALLBACK_FAMILY;
    }

    /**
     * Marks the start of a rendered frame. Atlases touched after this call are
     * pinned against budget eviction until the next frame; idle atlases from
     * earlier frames (for example after a GUI scale change) are released.
     */
    public synchronized void beginFrame() {
        long now = System.nanoTime();
        this.frameStartNanos = now;
        if (now - lastTrimNanos >= TRIM_INTERVAL_NANOS) {
            lastTrimNanos = now;
            evictIdle(now);
        }
    }

    public synchronized FontFace getFace(FontFamily family, float logicalSize,
                                         boolean bold, double guiScale) {
        if (family == null) {
            family = defaultFamily;
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
        int bakeHeight = bakeHeight(family, rasterHeight);

        long now = System.nanoTime();
        String resource = family.getResource(bold);
        AtlasKey atlasKey = new AtlasKey(resource, bakeHeight, family.isCjkCapable());
        FontAtlasEntry entry = atlases.get(atlasKey);
        if (entry == null) {
            entry = bake(atlasKey, now);
            atlases.put(atlasKey, entry);
            enforceBudget();
        }
        entry.lastUsedNanos = now;

        FaceKey faceKey = new FaceKey(atlasKey, logicalSize);
        FontFace face = faces.get(faceKey);
        if (face == null) {
            float drawScale = logicalSize / (float) bakeHeight;
            face = new FontFace(family, entry, logicalSize, drawScale, now);
            faces.put(faceKey, face);
        } else {
            face.touch(now);
        }
        return face;
    }

    private static int bakeHeight(FontFamily family, int rasterHeight) {
        if (!family.isCjkCapable()) {
            return rasterHeight;
        }
        for (int height : CJK_BAKE_HEIGHTS) {
            if (rasterHeight <= height) {
                return height;
            }
        }
        return CJK_BAKE_HEIGHTS[CJK_BAKE_HEIGHTS.length - 1];
    }

    private FontAtlasEntry bake(AtlasKey key, long now) {
        byte[] data = loadFont(key.resource);
        FontAtlas atlas = StbFontBaker.bake(
                data, key.bakeHeight, key.cjk, fullGbk);
        FontTexture texture = backend.createTexture(atlas);
        atlas.releasePixels();
        FontAtlasEntry entry = new FontAtlasEntry(
                key.resource, key.bakeHeight, atlas, texture, now);
        if (debugCache) {
            System.out.println("[OrangeX] Baked " + key.resource
                    + " @" + key.bakeHeight + "px -> "
                    + atlas.getWidth() + "x" + atlas.getHeight()
                    + " (" + (entry.texels * (long) bytesPerTexel / (1024 * 1024))
                    + " MB)");
        }
        return entry;
    }

    /**
     * Drops atlases and faces that have not been used for {@code idleSeconds}
     * (for example the previous GUI scale's faces).
     */
    private void evictIdle(long now) {
        Iterator<Map.Entry<AtlasKey, FontAtlasEntry>> iterator =
                atlases.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<AtlasKey, FontAtlasEntry> next = iterator.next();
            if (now - next.getValue().lastUsedNanos < idleNanos) {
                continue;
            }
            iterator.remove();
            close(next.getValue());
        }

        Iterator<Map.Entry<FaceKey, FontFace>> facesIterator =
                faces.entrySet().iterator();
        while (facesIterator.hasNext()) {
            if (now - facesIterator.next().getValue().getLastUsedNanos()
                    >= idleNanos) {
                facesIterator.remove();
            }
        }
    }

    /**
     * Evicts least recently used atlases until the texture budget is met.
     * Atlases already used in the current frame are pinned, so a working set
     * larger than the budget never causes per-frame re-baking.
     */
    private void enforceBudget() {
        long totalBytes = totalBytes();
        while (totalBytes > atlasBudgetBytes) {
            AtlasKey victimKey = null;
            FontAtlasEntry victim = null;
            for (Map.Entry<AtlasKey, FontAtlasEntry> candidate : atlases.entrySet()) {
                FontAtlasEntry entry = candidate.getValue();
                if (entry.lastUsedNanos >= frameStartNanos) {
                    continue;
                }
                if (victim == null || entry.lastUsedNanos < victim.lastUsedNanos) {
                    victim = entry;
                    victimKey = candidate.getKey();
                }
            }
            if (victim == null) {
                return;
            }
            atlases.remove(victimKey);
            totalBytes -= (long) victim.texels * bytesPerTexel;
            close(victim);
        }
    }

    private long totalBytes() {
        long total = 0L;
        for (FontAtlasEntry entry : atlases.values()) {
            total += (long) entry.texels * bytesPerTexel;
        }
        return total;
    }

    private void close(FontAtlasEntry entry) {
        entry.texture.close();
        Iterator<Map.Entry<FaceKey, FontFace>> iterator =
                faces.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getEntry() == entry) {
                iterator.remove();
            }
        }
        if (debugCache) {
            System.out.println("[OrangeX] Released " + entry.resource
                    + " @" + entry.bakeHeight + "px");
        }
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

    public synchronized void clear() {
        for (FontAtlasEntry entry : atlases.values()) {
            entry.texture.close();
        }
        atlases.clear();
        faces.clear();
    }

    private static final class AtlasKey {
        private final String resource;
        private final int bakeHeight;
        private final boolean cjk;

        private AtlasKey(String resource, int bakeHeight, boolean cjk) {
            this.resource = resource;
            this.bakeHeight = bakeHeight;
            this.cjk = cjk;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AtlasKey)) {
                return false;
            }
            AtlasKey key = (AtlasKey) other;
            return bakeHeight == key.bakeHeight
                    && cjk == key.cjk
                    && resource.equals(key.resource);
        }

        @Override
        public int hashCode() {
            int result = resource.hashCode();
            result = 31 * result + bakeHeight;
            result = 31 * result + (cjk ? 1 : 0);
            return result;
        }
    }

    private static final class FaceKey {
        private final AtlasKey atlasKey;
        private final float logicalSize;

        private FaceKey(AtlasKey atlasKey, float logicalSize) {
            this.atlasKey = atlasKey;
            this.logicalSize = logicalSize;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FaceKey)) {
                return false;
            }
            FaceKey key = (FaceKey) other;
            return Float.compare(logicalSize, key.logicalSize) == 0
                    && atlasKey.equals(key.atlasKey);
        }

        @Override
        public int hashCode() {
            return 31 * atlasKey.hashCode() + Float.floatToIntBits(logicalSize);
        }
    }
}
