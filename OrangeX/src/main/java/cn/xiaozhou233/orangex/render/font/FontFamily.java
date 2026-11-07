package cn.xiaozhou233.orangex.render.font;

import java.util.Locale;

/**
 * Bundled font families.
 *
 * <p>Every family maps a regular face and a bold face. Weights without a
 * dedicated bold file reuse a heavier (or the same) face, so asking for
 * {@code bold} text never fails to load.</p>
 *
 * <p>Families can be selected globally with
 * {@code -Dorangex.font.family=<name>}, where {@code <name>} is either the
 * display name ({@code "Montserrat Light"}), the constant name
 * ({@code MONTSERRAT_LIGHT}) or the file name ({@code Montserrat-Light.ttf}).
 * Matching ignores case, spaces and punctuation.</p>
 */
public enum FontFamily {

    PROXIMA("Proxima", "proxima.ttf", "proximabd.ttf", false),
    NOTO("Noto Sans", "noto.ttf", "noto.ttf", true),
    POPPINS("Poppins", "poppins_rg.ttf", "poppins_rg.ttf", false),
    JELLO("Jello", "JelloRegular.ttf", "JelloMedium.ttf", false),
    JELLO_LIGHT("Jello Light", "JelloLight.ttf", "JelloMedium.ttf", false),
    JELLO_MEDIUM("Jello Medium", "JelloMedium.ttf", "JelloMedium.ttf", false),
    MONTSERRAT("Montserrat", "Montserrat-Regular.ttf", "Montserrat-Bold.ttf", false),
    MONTSERRAT_LIGHT("Montserrat Light", "Montserrat-Light.ttf", "Montserrat-Bold.ttf", false),
    MONTSERRAT_MEDIUM("Montserrat Medium", "Montserrat-Medium.ttf", "Montserrat-Bold.ttf", false);

    private static final String FONT_DIRECTORY = "assets/orangex/font/";

    private final String displayName;
    private final String regularResource;
    private final String boldResource;
    private final boolean cjkCapable;

    private final String constantKey;
    private final String displayKey;
    private final String fileKey;

    FontFamily(String displayName, String regularFile, String boldFile,
               boolean cjkCapable) {
        this.displayName = displayName;
        this.regularResource = FONT_DIRECTORY + regularFile;
        this.boldResource = FONT_DIRECTORY + boldFile;
        this.cjkCapable = cjkCapable;

        this.constantKey = normalize(name());
        this.displayKey = normalize(displayName);
        this.fileKey = normalize(stripExtension(regularFile));
    }

    /**
     * Human readable name, suitable for configuration screens.
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getRegularResource() {
        return regularResource;
    }

    public String getBoldResource() {
        return boldResource;
    }

    public String getResource(boolean bold) {
        return bold ? boldResource : regularResource;
    }

    public boolean isCjkCapable() {
        return cjkCapable;
    }

    /**
     * Resolves a family from its display name, constant name or font file
     * name. Matching is case-insensitive and ignores spaces/punctuation.
     *
     * @return the matching family, or {@code null} when nothing matches.
     */
    public static FontFamily byName(String name) {
        if (name == null) {
            return null;
        }
        String key = normalize(stripExtension(name));
        if (key.isEmpty()) {
            return null;
        }
        for (FontFamily family : values()) {
            if (key.equals(family.constantKey)
                    || key.equals(family.displayKey)
                    || key.equals(family.fileKey)) {
                return family;
            }
        }
        return null;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
