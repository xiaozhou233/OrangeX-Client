package cn.xiaozhou233.orangex.mapping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingData {
    // Readable -> Obfuscated
    private final Map<String, String> className;
    private final Map<String, String> methodName;
    private final Map<String, String> fieldName;

    // Obfuscated -> Readable
    private final Map<String, String> classNameReversed;
    private final Map<String, String> methodNameReversed;
    private final Map<String, String> fieldNameReversed;

    public MappingData() {
        this.className = new LinkedHashMap<>();
        this.methodName = new LinkedHashMap<>();
        this.fieldName = new LinkedHashMap<>();

        this.classNameReversed = new LinkedHashMap<>();
        this.methodNameReversed = new LinkedHashMap<>();
        this.fieldNameReversed = new LinkedHashMap<>();
    }

    // Add

    public void addClass(String obfuscated, String readable) {
        className.put(readable, obfuscated);
        classNameReversed.put(obfuscated, readable);
    }

    public void addMethod(String obfuscated, String readable) {
        methodName.put(readable, obfuscated);
        methodNameReversed.put(obfuscated, readable);
    }

    public void addField(String obfuscated, String readable) {
        fieldName.put(readable, obfuscated);
        fieldNameReversed.put(obfuscated, readable);
    }

    // Lookup

    public String lookupClass(String readable) {
        return className.get(readable);
    }

    public String lookupMethod(String readable) {
        return methodName.get(readable);
    }

    public String lookupField(String readable) {
        return fieldName.get(readable);
    }

    // Reverse Lookup
    public String reverseClass(String obfuscated) {
        return classNameReversed.get(obfuscated);
    }

    public String reverseMethod(String obfuscated) {
        return methodNameReversed.get(obfuscated);
    }

    public String reverseField(String obfuscated) {
        return fieldNameReversed.get(obfuscated);
    }

    // Getters

    public Map<String, String> getClassName() {
        return Collections.unmodifiableMap(className);
    }

    public Map<String, String> getMethodName() {
        return Collections.unmodifiableMap(methodName);
    }

    public Map<String, String> getFieldName() {
        return Collections.unmodifiableMap(fieldName);
    }
}