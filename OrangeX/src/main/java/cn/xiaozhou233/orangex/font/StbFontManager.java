package cn.xiaozhou233.orangex.font;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StbFontManager {
    private byte[] proximaData;
    private byte[] proximaBoldData;
    private byte[] notoData;
    private byte[] poppinsData;

    private final ConcurrentMap<Integer, SmoothFontRenderer> regularFonts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, SmoothFontRenderer> boldFonts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, SmoothFontRenderer> notoFonts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, SmoothFontRenderer> poppinsFonts = new ConcurrentHashMap<>();

    private boolean initialized = false;

    public void loadFonts() {
        this.proximaData = readResource("proxima.ttf");
        this.proximaBoldData = readResource("proximabd.ttf");
        this.notoData = readResource("noto.ttf");
        this.poppinsData = readResource("poppins_rg.ttf");
        this.initialized = true;
    }

    private byte[] readResource(String path) {
        try {
            InputStream is = StbFontManager.class.getClassLoader().getResourceAsStream("fonts/" + path);
            if (is == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            is.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SmoothFontRenderer getProxima(int size) {
        return getFont(regularFonts, proximaData, size, "Proxima");
    }

    public SmoothFontRenderer getProximaBold(int size) {
        return getFont(boldFonts, proximaBoldData, size, "Proxima Bold");
    }

    public SmoothFontRenderer getNoto(int size) {
        return getFont(notoFonts, notoData, size, "Noto");
    }

    public SmoothFontRenderer getPoppins(int size) {
        return getFont(poppinsFonts, poppinsData, size, "Poppins");
    }

    private SmoothFontRenderer getFont(ConcurrentMap<Integer, SmoothFontRenderer> cache, byte[] data, int size, String name) {
        if (!cache.containsKey(size)) {
            if (data == null) return null;
            LegacySmoothFontRenderer renderer = new LegacySmoothFontRenderer(data, size, name);
            cache.put(size, renderer);
            return renderer;
        }
        return cache.get(size);
    }
}