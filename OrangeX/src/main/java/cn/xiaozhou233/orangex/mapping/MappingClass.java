package cn.xiaozhou233.orangex.mapping;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MappingClass {

    @Getter
    private final String sourceName;
    @Getter
    private final String targetName;

    private final Map<String, MappingField> fields = new HashMap<>();
    private final Map<String, MappingMethod> methods = new HashMap<>();

    /**
     * Identity mapping: named and target names are the same for every member,
     * including members that were never registered explicitly.
     */
    private final boolean identity;

    public MappingClass(String sourceName, String targetName) {
        this(sourceName, targetName, false);
    }

    private MappingClass(String sourceName, String targetName, boolean identity) {
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.identity = identity;
    }

    /**
     * Creates a class mapping that resolves any field or method to itself.
     * Used by environments that already run with named (MCP) members.
     */
    public static MappingClass identity(String className) {
        return new MappingClass(className, className, true);
    }

    public void addField(MappingField field) {
        fields.put(field.getSourceName(), field);
    }

    public void addMethod(MappingMethod method) {
        methods.put(
                method.getSourceName() + '\0' + method.getSourceDescriptor(),
                method
        );
    }

    public MappingField getField(String name) {
        MappingField field = fields.get(name);
        if (field != null || !identity) {
            return field;
        }
        return new MappingField(name, name);
    }

    public MappingMethod getMethod(String name, String descriptor) {
        MappingMethod method = methods.get(name + '\0' + descriptor);
        if (method != null || !identity) {
            return method;
        }
        return new MappingMethod(name, descriptor, name, descriptor);
    }

    public Map<String, MappingField> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public Map<String, MappingMethod> getMethods() {
        return Collections.unmodifiableMap(methods);
    }
}