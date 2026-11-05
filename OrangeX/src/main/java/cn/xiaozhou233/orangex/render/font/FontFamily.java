package cn.xiaozhou233.orangex.render.font;

/**
 * Bundled font families.
 */
public enum FontFamily {

    PROXIMA("assets/orangex/font/proxima.ttf", "assets/orangex/font/proximabd.ttf", false),
    NOTO("assets/orangex/font/noto.ttf", "assets/orangex/font/noto.ttf", true),
    POPPINS("assets/orangex/font/poppins_rg.ttf", "assets/orangex/font/poppins_rg.ttf", false);

    private final String regularResource;
    private final String boldResource;
    private final boolean cjkCapable;

    FontFamily(String regularResource, String boldResource, boolean cjkCapable) {
        this.regularResource = regularResource;
        this.boldResource = boldResource;
        this.cjkCapable = cjkCapable;
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
}
