package cn.xiaozhou233.orangex.mapping;


public final class MappingReader {
    private static final MappingReader INSTANCE = new MappingReader();
    private MappingData data;

    private MappingReader() {
    }

    public static MappingReader getInstance() {
        return INSTANCE;
    }

}