
package cn.xiaozhou233.orangex.mapping.impl;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import cn.xiaozhou233.orangex.mapping.MappingResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SrgMappingResolver implements MappingResolver {

    private final Map<String, MappingClass> classes = new HashMap<>();

    public SrgMappingResolver(String srgFile) {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(srgFile);

        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "SRG file not found: " + srgFile
            );
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    parseLine(line);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse SRG at line "
                                    + lineNumber + ": " + line,
                            e
                    );
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read SRG file: " + srgFile,
                    e
            );
        }
    }

    private void parseLine(String line) {
        String[] parts = line.split("\\s+");

        switch (parts[0]) {
            case "CL:":
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid CL entry");
                }

                // SRG: obfuscated -> named
                // Internal: named -> obfuscated
                getOrCreateClass(parts[2], parts[1]);
                break;

            case "FD:":
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid FD entry");
                }

                String[] sourceField = splitMember(parts[1]);
                String[] targetField = splitMember(parts[2]);

                getOrCreateClass(
                        targetField[0],
                        sourceField[0]
                ).addField(new MappingField(
                        targetField[1],
                        sourceField[1]
                ));

                break;

            case "MD:":
                if (parts.length != 5) {
                    throw new IllegalArgumentException("Invalid MD entry");
                }

                String[] sourceMethod = splitMember(parts[1]);
                String[] targetMethod = splitMember(parts[3]);

                getOrCreateClass(
                        targetMethod[0],
                        sourceMethod[0]
                ).addMethod(new MappingMethod(
                        targetMethod[1],
                        parts[4],
                        sourceMethod[1],
                        parts[2]
                ));

                break;

            default:
                // Ignore unsupported entries.
                break;
        }
    }

    private MappingClass getOrCreateClass(
            String sourceName,
            String targetName
    ) {
        MappingClass mappingClass = classes.get(sourceName);

        if (mappingClass == null) {
            mappingClass = new MappingClass(sourceName, targetName);
            classes.put(sourceName, mappingClass);
        }

        return mappingClass;
    }

    private String[] splitMember(String member) {
        int index = member.lastIndexOf('/');

        if (index < 0 || index == member.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid member: " + member
            );
        }

        return new String[]{
                member.substring(0, index),
                member.substring(index + 1)
        };
    }

    public MappingClass getMappingClass(String className) {
        return mapClass(className);
    }

    @Override
    public MappingClass mapClass(String className) {
        MappingClass mappingClass = classes.get(className);

        if (mappingClass != null) {
            return mappingClass;
        }

        // Return identity mapping when class is not found.
        return new MappingClass(className, className);
    }

    @Override
    public MappingField mapField(String owner, String fieldName) {
        MappingClass mappingClass = classes.get(owner);

        if (mappingClass != null) {
            MappingField mappingField = mappingClass.getField(fieldName);

            if (mappingField != null) {
                return mappingField;
            }
        }

        // Return identity mapping when field is not found.
        return new MappingField(fieldName, fieldName);
    }

    @Override
    public MappingMethod mapMethod(
            String owner,
            String methodName,
            String descriptor
    ) {
        MappingClass mappingClass = classes.get(owner);

        if (mappingClass != null) {
            MappingMethod mappingMethod =
                    mappingClass.getMethod(methodName, descriptor);

            if (mappingMethod != null) {
                return mappingMethod;
            }
        }

        // Return identity mapping when method is not found.
        return new MappingMethod(
                methodName,
                descriptor,
                methodName,
                descriptor
        );
    }
}
