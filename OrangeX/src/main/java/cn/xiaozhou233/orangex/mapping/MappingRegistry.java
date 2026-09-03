package cn.xiaozhou233.orangex.mapping;

import java.util.HashMap;
import java.util.Map;

public class MappingRegistry {
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> fieldMappings = new HashMap<>();
    private final Map<String, String> methodMappings = new HashMap<>();

    public void registerClass(String sourceName, String runtimeName) {
        classMappings.put(sourceName, runtimeName);
    }

    public void registerField(String ownerAndName, String runtimeName) {
        fieldMappings.put(ownerAndName, runtimeName);
    }

    public void registerMethod(String ownerAndDescriptor, String runtimeName) {
        methodMappings.put(ownerAndDescriptor, runtimeName);
    }

    public void registerField(String ownerName, String fieldName, String runtimeName) {
        fieldMappings.put(ownerName + "/" + fieldName, runtimeName);
    }

    public void registerMethod(String ownerName, String methodName, String descriptor, String runtimeName) {
        methodMappings.put(ownerName + "/" + methodName + " " + descriptor, runtimeName);
    }

    public String lookupClass(String sourceName) {
        return classMappings.get(sourceName);
    }

    public String lookupField(String ownerName, String fieldName) {
        String result = fieldMappings.get(ownerName + "/" + fieldName);
        if (result == null) {
            result = fieldMappings.get(fieldName);
        }
        return result;
    }

    public String lookupMethod(String ownerName, String methodName, Class<?>[] paramTypes) {
        String descriptor = buildDescriptor(paramTypes);
        String result = methodMappings.get(ownerName + "/" + methodName + " " + descriptor);
        if (result == null) {
            result = methodMappings.get(methodName + " " + descriptor);
        }
        if (result == null) {
            result = methodMappings.get(methodName);
        }
        return result;
    }

    private static String buildDescriptor(Class<?>[] paramTypes) {
        if (paramTypes == null || paramTypes.length == 0) {
            return "()";
        }
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> type : paramTypes) {
            if (type == Integer.TYPE) sb.append('I');
            else if (type == Boolean.TYPE) sb.append('Z');
            else if (type == Float.TYPE) sb.append('F');
            else if (type == Double.TYPE) sb.append('D');
            else if (type == Long.TYPE) sb.append('J');
            else if (type == Short.TYPE) sb.append('S');
            else if (type == Byte.TYPE) sb.append('B');
            else if (type == Character.TYPE) sb.append('C');
            else if (type == Void.TYPE) sb.append('V');
            else sb.append('L').append(type.getName().replace('.', '/')).append(';');
        }
        sb.append(')');
        return sb.toString();
    }

    public void loadFrom(MappingData data) {
        for (Map.Entry<String, String> entry : data.getClassName().entrySet()) {
            classMappings.put(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, String> entry : data.getFieldName().entrySet()) {
            fieldMappings.put(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, String> entry : data.getMethodName().entrySet()) {
            methodMappings.put(entry.getValue(), entry.getKey());
        }
    }

    public void loadIdentityFor(String mcpClassName) {
        String internalName = mcpClassName.replace('.', '/');
        classMappings.put(internalName, internalName);
    }

    public void clear() {
        classMappings.clear();
        fieldMappings.clear();
        methodMappings.clear();
    }
}