package cn.xiaozhou233.orangex.render.internal.v26_2;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Creates a {@code DynamicTexture} without linking against Mojang's classes.
 *
 * <p>Two input formats are supported so callers cannot mix them up: raw RGBA
 * (shapes, sprites) and one-byte coverage/font atlases, which are expanded to
 * white RGB + coverage alpha directly inside the {@code NativeImage} buffer.</p>
 */
final class V26_2TextureHelper {

    static V26_2GpuTexture create(ClassLoader loader, int width, int height,
                                  final byte[] rgba) {
        return create(loader, width, height, new PixelWriter() {
            @Override
            public void write(ByteBuffer buffer) {
                buffer.put(rgba);
            }
        });
    }

    static V26_2GpuTexture createFromCoverage(ClassLoader loader, int width,
                                              int height, final byte[] coverage) {
        return create(loader, width, height, new PixelWriter() {
            @Override
            public void write(ByteBuffer buffer) {
                // Expand straight into the NativeImage buffer: avoids a second
                // full-size RGBA array on top of the atlas and the image.
                for (byte alpha : coverage) {
                    buffer.put((byte) 0xFF);
                    buffer.put((byte) 0xFF);
                    buffer.put((byte) 0xFF);
                    buffer.put(alpha);
                }
            }
        });
    }

    private static V26_2GpuTexture create(ClassLoader loader, int width,
                                          int height, PixelWriter writer) {
        try {
            Class<?> nativeImageClass = Class.forName(
                    "com.mojang.blaze3d.platform.NativeImage", false, loader);
            Constructor<?> nativeImageConstructor = nativeImageClass
                    .getDeclaredConstructor(int.class, int.class, boolean.class);
            Object image = nativeImageConstructor.newInstance(width, height, false);

            ByteBuffer buffer = (ByteBuffer) nativeImageClass
                    .getMethod("getPixelBytes").invoke(image);
            writer.write(buffer);

            Class<?> dynamicTextureClass = Class.forName(
                    "net.minecraft.client.renderer.texture.DynamicTexture", false, loader);
            Constructor<?> dynamicTextureConstructor = dynamicTextureClass
                    .getDeclaredConstructor(Supplier.class, nativeImageClass);
            Object texture = dynamicTextureConstructor.newInstance(
                    new Supplier<String>() {
                        @Override
                        public String get() {
                            return "OrangeX texture";
                        }
                    },
                    image);

            Object textureView = dynamicTextureClass.getMethod("getTextureView")
                    .invoke(texture);
            Object sampler = dynamicTextureClass.getMethod("getSampler")
                    .invoke(texture);
            return new V26_2GpuTexture(texture, textureView, sampler, width, height);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Cannot create a 26.2 texture", throwable);
        }
    }

    private interface PixelWriter {
        void write(ByteBuffer buffer);
    }

    private V26_2TextureHelper() {
    }
}
