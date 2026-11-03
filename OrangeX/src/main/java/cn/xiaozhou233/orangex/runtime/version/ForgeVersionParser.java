package cn.xiaozhou233.orangex.runtime.version;

/**
 * Parses Forge/NeoForge version strings into OrangeX internal version ids.
 *
 * <p>Some Forge lines use ids that differ from Minecraft's own numbering, so a
 * couple of values are remapped explicitly. Any other value is accepted only
 * when it matches a known {@link MinecraftVersion}.</p>
 */
public final class ForgeVersionParser {

    private static final int NOT_FOUND = -1;

    public static int parse(Object value) {
        if (value == null) {
            return NOT_FOUND;
        }

        String text = String.valueOf(value).trim();
        int start = 0;
        while (start < text.length() && !Character.isDigit(text.charAt(start))) {
            start++;
        }

        int end = start;
        while (end < text.length() && Character.isDigit(text.charAt(end))) {
            end++;
        }

        if (start == end) {
            return NOT_FOUND;
        }

        try {
            int parsed = Integer.parseInt(text.substring(start, end));

            if (parsed == 62) {
                return MinecraftVersion.V26_1.getId();
            }
            if (parsed == 65) {
                return MinecraftVersion.V26_2.getId();
            }

            return MinecraftVersion.fromId(parsed).isUnknown() ? NOT_FOUND : parsed;
        } catch (NumberFormatException ignored) {
            return NOT_FOUND;
        }
    }

    private ForgeVersionParser() {
    }
}
