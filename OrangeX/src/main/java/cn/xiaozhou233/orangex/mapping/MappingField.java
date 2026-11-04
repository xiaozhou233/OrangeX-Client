package cn.xiaozhou233.orangex.mapping;

public class MappingField {

    private final String sourceName;
    private final String targetName;

    public MappingField(String sourceName, String targetName) {
        this.sourceName = sourceName;
        this.targetName = targetName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTargetName() {
        return targetName;
    }
}