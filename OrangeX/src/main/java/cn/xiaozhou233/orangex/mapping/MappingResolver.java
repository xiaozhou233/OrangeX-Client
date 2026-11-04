package cn.xiaozhou233.orangex.mapping;

public interface MappingResolver {

    MappingClass mapClass(String className);

    MappingField mapField(String owner, String fieldName);

    MappingMethod mapMethod(
            String owner,
            String methodName,
            String descriptor
    );

}