package analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件读取工具。
 */
public final class FileUtil {
    private FileUtil() {
    }

    public static String readText(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
