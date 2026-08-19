package cn.xiaozhou233.orangex.font;

import java.awt.*;
import java.io.InputStream;


public class FontUtil {

    public static Font getFontFromTTF(InputStream inputStream, float fontSize, int fontType) {
        Font output = null;
        try {
            output = Font.createFont(fontType, inputStream);
            output = output.deriveFont(fontSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
