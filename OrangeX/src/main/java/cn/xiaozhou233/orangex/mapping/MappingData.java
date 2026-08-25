package cn.xiaozhou233.orangex.mapping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingData {
    private final Map<String, String> classNames;
    private final Map<String, String> classNamesReversed;
    private final Map<String, String> fieldNames;
    private final Map<String, String> fieldNamesReversed;
    private final Map<String, String> methodNames;
    private final Map<String, String> methodNamesReversed;

    public MappingData() {
        this.classNames = new LinkedHashMap<>();
        this.classNamesReversed = new LinkedHashMap<>();
        this.fieldNames = new LinkedHashMap<>();
        this.fieldNamesReversed = new LinkedHashMap<>();
        this.methodNames = new LinkedHashMap<>();
        this.methodNamesReversed = new LinkedHashMap<>();
    }

    public void addClass(String obfuscated, String readable) {
        classNames.put(obfuscated, readable);
        classNamesReversed.put(readable, obfuscated);
    }

    public void addField(String obfuscated, String readable) {
        fieldNames.put(obfuscated, readable);
        fieldNamesReversed.put(readable, obfuscated);
    }

    public void addMethod(String obfuscated, String readable) {
        methodNames.put(obfuscated, readable);
        methodNamesReversed.put(readable, obfuscated);
    }

    public String lookupClass(String obfuscated) {
        return classNames.get(obfuscated);
    }

    public String reverseClass(String readable) {
        return classNamesReversed.get(readable);
    }

    public String lookupField(String obfuscated) {
        return fieldNames.get(obfuscated);
    }

    public String reverseField(String readable) {
        return fieldNamesReversed.get(readable);
    }

    public String lookupMethod(String obfuscated) {
        return methodNames.get(obfuscated);
    }

    public String reverseMethod(String readable) {
        return methodNamesReversed.get(readable);
    }

    public Map<String, String> getClassNames() {
        return Collections.unmodifiableMap(classNames);
    }

    public Map<String, String> getFieldNames() {
        return Collections.unmodifiableMap(fieldNames);
    }

    public Map<String, String> getMethodNames() {
        return Collections.unmodifiableMap(methodNames);
    }

    public boolean isEmpty() {
        return classNames.isEmpty() && fieldNames.isEmpty() && methodNames.isEmpty();
    }
}