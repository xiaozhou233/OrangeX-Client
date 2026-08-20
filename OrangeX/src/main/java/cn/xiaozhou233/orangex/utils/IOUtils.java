package cn.xiaozhou233.orangex.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    public static byte[] toByteArray(InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }

        byte[] buffer = new byte[8192]; // 8KB buffer
        int len;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }

        return output.toByteArray();
    }
}