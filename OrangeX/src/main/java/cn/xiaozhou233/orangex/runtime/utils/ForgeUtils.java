package cn.xiaozhou233.orangex.runtime.utils;

public class ForgeUtils {
    public static int parseForgeVersion(Object value) {
        if (value == null) {
            return -1;
        }
        String text = String.valueOf(value).trim();
        int start = 0;
        while (start < text.length() && !Character.isDigit(text.charAt(start))) {
            ++start;
        }
        int end = start;
        while (end < text.length() && Character.isDigit(text.charAt(end))) {
            ++end;
        }
        if (start == end) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(text.substring(start, end));
            if (parsed == 62) {
                return 100;
            }
            if (parsed == 65) {
                return 110;
            }
            return isKnownVersionId(parsed) ? parsed : -1;
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static boolean isKnownVersionId(int version) {
        switch (version) {
            case 13: case 15: case 23: case 28: case 35: case 36: case 37:
            case 47: case 50: case 51: case 52: case 54: case 55: case 56: case 60: case 61:
            case 100: case 110:
                return true;
            default:
                return false;
        }
    }
}
