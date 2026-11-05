package cn.xiaozhou233.orangex.render.internal.v26_2;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * Creates a {@code DynamicTexture} from an RGBA byte array without linking
 * against Mojang's classes.
 */
final class V26_2TextureHelper {

    static V26_2GpuTexture create(ClassLoader loader, int width, int height,
                                  byte[] rgba) {
        try {
            Class<?> nativeImageClass = Class.forName(
                    "com.mojang.blaze3d.platform.NativeImage", false, loader);
            Constructor<?> nativeImageConstructor = nativeImageClass
                    .getDeclaredConstructor(int.class, int.class, boolean.class);
            Object image = nativeImageConstructor.newInstance(width, height, false);

            ByteBuffer buffer = (ByteBuffer) nativeImageClass
                    .getMethod("getPixelBytes").invoke(image);
            buffer.put(rgba);

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

    private V26_2TextureHelper() {
    }
}
