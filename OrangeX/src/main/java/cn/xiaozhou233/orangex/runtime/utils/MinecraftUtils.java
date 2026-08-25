package cn.xiaozhou233.orangex.runtime.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MinecraftUtils {
    public static int matchesVersionMetadata(Class<?> minecraftClass) {
        try (InputStream stream = minecraftClass.getResourceAsStream(
                "/version.json")) {
            if (stream == null) {
                return 0;
            }
            JsonElement element = new JsonParser().parse(new InputStreamReader(
                    stream, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                return 0;
            }
            JsonObject version = element.getAsJsonObject();
            if (getInteger(version, "java_version") != 25) {
                return 0;
            }
            String id = getString(version, "id");
            String name = getString(version, "name");
            if (id != null && id.equals(name)) {
                boolean is261Family = id.startsWith("26.1")
                        && (id.length() == 4 || id.charAt(4) == '.');
                boolean is262Family = id.startsWith("26.2")
                        && (id.length() == 4 || id.charAt(4) == '.');
                if (is261Family) {
                    return 100;
                }
                if (is262Family) {
                    return 110;
                }
            }
            return 0;
        }
        catch (Exception ignored) {
            return 0;
        }
        catch (LinkageError ignored) {
            return 0;
        }
    }

    private static String getString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive()
                ? value.getAsString() : null;
    }

    private static int getInteger(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive()
                ? value.getAsInt() : Integer.MIN_VALUE;
    }

}
