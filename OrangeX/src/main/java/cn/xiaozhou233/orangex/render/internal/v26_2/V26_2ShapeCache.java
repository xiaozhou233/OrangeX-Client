package cn.xiaozhou233.orangex.render.internal.v26_2;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches white quarter-circle corner textures used to assemble rounded
 * rectangles through the 26.x GUI blit pipeline.
 */
final class V26_2ShapeCache {

    private final ClassLoader loader;
    private final Map<Integer, V26_2GpuTexture> corners =
            new HashMap<Integer, V26_2GpuTexture>();

    V26_2ShapeCache(ClassLoader loader) {
        this.loader = loader;
    }

    synchronized V26_2GpuTexture corner(int radiusPixels) {
        int radius = Math.max(2, radiusPixels);
        V26_2GpuTexture cached = corners.get(radius);
        if (cached != null) {
            return cached;
        }

        int size = radius;
        byte[] rgba = new byte[size * size * 4];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = radius - (x + 0.5);
                double dy = radius - (y + 0.5);
                double distance = Math.sqrt(dx * dx + dy * dy);
                float coverage = (float) clamp(radius - distance + 0.5, 0.0, 1.0);

                int offset = (y * size + x) * 4;
                rgba[offset] = (byte) 0xFF;
                rgba[offset + 1] = (byte) 0xFF;
                rgba[offset + 2] = (byte) 0xFF;
                rgba[offset + 3] = (byte) Math.round(coverage * 255.0f);
            }
        }

        V26_2GpuTexture texture = V26_2TextureHelper.create(
                loader, size, size, rgba);
        corners.put(radius, texture);
        return texture;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
