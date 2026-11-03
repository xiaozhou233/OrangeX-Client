package cn.xiaozhou233.orangex.runtime.utils;

import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Reads metadata bundled with the running Minecraft client.
 */
public final class MinecraftUtils {

    /**
     * Reads {@code /version.json} and maps the 26.x version family to an
     * internal version id.
     *
     * @return the internal id, or {@link MinecraftVersion#UNKNOWN_ID} when the
     *         metadata is missing or does not describe a 26.x client
     */
    public static int readModernVersionId(Class<?> minecraftClass) {
        try (InputStream stream = minecraftClass.getResourceAsStream("/version.json")) {
            if (stream == null) {
                return MinecraftVersion.UNKNOWN_ID;
            }

            JsonElement element = JsonParser.parseReader(new InputStreamReader(
                    stream, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                return MinecraftVersion.UNKNOWN_ID;
            }

            JsonObject version = element.getAsJsonObject();
            int javaVersion = getInteger(version, "java_version");
            if (javaVersion != Integer.MIN_VALUE && javaVersion < 25) {
                return MinecraftVersion.UNKNOWN_ID;
            }

            String id = getString(version, "id");
            String name = getString(version, "name");
            if (id == null || !id.equals(name)) {
                return MinecraftVersion.UNKNOWN_ID;
            }

            if (isFamily(id, "26.1")) {
                return MinecraftVersion.V26_1.getId();
            }
            if (isFamily(id, "26.2")) {
                return MinecraftVersion.V26_2.getId();
            }
            return MinecraftVersion.UNKNOWN_ID;
        } catch (Exception | LinkageError ignored) {
            return MinecraftVersion.UNKNOWN_ID;
        }
    }

    private static boolean isFamily(String id, String family) {
        return id.startsWith(family)
                && (id.length() == family.length() || id.charAt(family.length()) == '.');
    }

    private static String getString(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    private static int getInteger(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() : Integer.MIN_VALUE;
    }

    private MinecraftUtils() {
    }
}
