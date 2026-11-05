package cn.xiaozhou233.orangex.render.internal.v26_2;

/**
 * Reflective holder for a 26.x GPU texture and its sampler.
 */
final class V26_2GpuTexture {

    final Object texture;
    final Object textureView;
    final Object sampler;
    final int width;
    final int height;

    V26_2GpuTexture(Object texture, Object textureView, Object sampler,
                    int width, int height) {
        this.texture = texture;
        this.textureView = textureView;
        this.sampler = sampler;
        this.width = width;
        this.height = height;
    }
}
