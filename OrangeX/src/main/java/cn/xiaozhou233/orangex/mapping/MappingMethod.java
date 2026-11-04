package cn.xiaozhou233.orangex.mapping;

public class MappingMethod {

    private final String sourceName;
    private final String sourceDescriptor;

    private final String targetName;
    private final String targetDescriptor;

    public MappingMethod(
            String sourceName,
            String sourceDescriptor,
            String targetName,
            String targetDescriptor
    ) {
        this.sourceName = sourceName;
        this.sourceDescriptor = sourceDescriptor;
        this.targetName = targetName;
        this.targetDescriptor = targetDescriptor;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceDescriptor() {
        return sourceDescriptor;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetDescriptor() {
        return targetDescriptor;
    }
}