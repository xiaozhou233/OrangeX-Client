package cn.xiaozhou233.orangex.injector;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static void unzip(InputStream zipInputStream, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs(); // 创建目标目录
        }

        try (ZipInputStream zipIn = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs(); // 创建目录
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs(); // 创建父目录
                    }

                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }
}
