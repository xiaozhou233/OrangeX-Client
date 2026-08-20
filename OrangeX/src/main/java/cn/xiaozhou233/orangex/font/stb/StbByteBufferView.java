package cn.xiaozhou233.orangex.font.stb;

public final class StbByteBufferView {
    public byte[] f;
    public int y;
    public int C;
    public int h;

    public StbByteBufferView() {
        this(null, 0, 0);
    }

    public StbByteBufferView(byte[] data, int offset, int length) {
        this.f = data;
        this.C = offset;
        this.h = 0;
        this.y = length;
    }

    public StbByteBufferView k() {
        StbByteBufferView copy = new StbByteBufferView();
        copy.f = this.f;
        copy.C = this.C;
        copy.h = this.h;
        copy.y = this.y;
        return copy;
    }
}