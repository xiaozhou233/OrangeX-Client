package cn.xiaozhou233.orangex.mapping;

import cn.xiaozhou233.orangex.runtime.loader.LoaderDetector;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class MappingReader {
    private static final MappingReader INSTANCE = new MappingReader();
    private MappingData currentData;

    private MappingReader() {
    }

    public static MappingReader getInstance() {
        return INSTANCE;
    }

    public MappingData load(LoaderType loaderType, int protocolVersion) {
        MappingData data = new MappingData();
        String versionDir = String.valueOf(protocolVersion);
        String loaderDir = loaderDirName(loaderType);

        readJoinedSrg(versionDir, loaderDir, data);
        readForgeCsv(versionDir, loaderDir, data);
        readNeoForgeMaps(versionDir, loaderDir, data);

        currentData = data;
        return data;
    }

    public MappingData load() {
        int protocol = MinecraftVersion.getCurrentProtocolVersion();
        if (protocol == 0) {
            return new MappingData();
        }
        LoaderType loader = LoaderDetector.detect(
                Thread.currentThread().getContextClassLoader());
        return load(loader, protocol);
    }

    public MappingData getCurrentData() {
        if (currentData == null) {
            return load();
        }
        return currentData;
    }

    private static String loaderDirName(LoaderType type) {
        switch (type) {
            case VANILLA:
            case MCP:
                return "vanilla";
            case FORGE:
                return "forge";
            case NEOFORGE:
                return "neoforge";
            case FABRIC:
                return "fabric";
            default:
                return "vanilla";
        }
    }

    private String resourcePath(String versionDir, String loaderDir, String file) {
        return "/mappings/" + versionDir + "/" + loaderDir + "/" + file;
    }

    private void readJoinedSrg(String versionDir, String loaderDir, MappingData data) {
        String path = resourcePath(versionDir, loaderDir, "joined.srg");
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) return;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("CL: ")) {
                        String[] columns = line.split("\\s+");
                        if (columns.length == 3) {
                            data.addClass(columns[1], columns[2]);
                        }
                    } else if (line.startsWith("FD: ")) {
                        String[] columns = line.split("\\s+");
                        if (columns.length == 3) {
                            String obfuscated = columns[1];
                            String readable = simpleMemberName(columns[2]);
                            data.addField(obfuscated, readable);
                        }
                    } else if (line.startsWith("MD: ")) {
                        String[] columns = line.split("\\s+");
                        if (columns.length == 5) {
                            String obfuscated = columns[1] + ' ' + columns[2];
                            String readable = simpleMemberName(columns[3]);
                            data.addMethod(obfuscated, readable);
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void readForgeCsv(String versionDir, String loaderDir, MappingData data) {
        readCsv(versionDir, loaderDir, "fields.csv", data, true);
        readCsv(versionDir, loaderDir, "methods.csv", data, false);
    }

    private void readCsv(String versionDir, String loaderDir, String file,
                         MappingData data, boolean isField) {
        String path = resourcePath(versionDir, loaderDir, file);
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) return;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length < 2) continue;
                    String srgName = columns[0];
                    String readableName = columns[1];
                    if (isField) {
                        data.addField(srgName, readableName);
                    } else {
                        data.addMethod(srgName, readableName);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void readNeoForgeMaps(String versionDir, String loaderDir, MappingData data) {
        readObfMembersMap(versionDir, loaderDir, data);
        readFieldsMap(versionDir, loaderDir, data);
    }

    private void readObfMembersMap(String versionDir, String loaderDir, MappingData data) {
        String path = resourcePath(versionDir, loaderDir, "obfmembers.map");
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) return;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    char kind = line.charAt(0);
                    if (kind != 'M' && kind != 'F') continue;
                    int first = line.indexOf(' ');
                    int second = line.indexOf(' ', first + 1);
                    if (first < 0 || second < 0) continue;
                    String mojmapKey = line.substring(first + 1, second);
                    String obfuscatedName = line.substring(second + 1);
                    if (kind == 'F') {
                        data.addField(obfuscatedName, mojmapKey);
                    } else {
                        data.addMethod(obfuscatedName, mojmapKey);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void readFieldsMap(String versionDir, String loaderDir, MappingData data) {
        String path = resourcePath(versionDir, loaderDir, "fields.map");
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) return;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    int space = line.indexOf(' ');
                    if (space > 0) {
                        String obfuscatedKey = line.substring(0, space);
                        String mojmapName = line.substring(space + 1);
                        data.addField(obfuscatedKey, mojmapName);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static String simpleMemberName(String qualifiedName) {
        int separator = qualifiedName.lastIndexOf('/');
        return separator < 0 ? qualifiedName : qualifiedName.substring(separator + 1);
    }
}