package cn.xiaozhou233.orangex.mapping.impl;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;

public class MCPMappingResolver implements MappingResolver {

    @Override
    public MappingClass mapClass(String className) {
        return MappingClass.identity(className);
    }

    @Override
    public MappingField mapField(String owner, String fieldName) {
        return new MappingField(fieldName, fieldName);
    }

    @Override
    public MappingMethod mapMethod(
            String owner,
            String methodName,
            String descriptor
    ) {
        return new MappingMethod(
                methodName,
                descriptor,
                methodName,
                descriptor
        );
    }
}