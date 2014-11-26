package cn.mibcxb.android.util;

import java.io.File;
import java.io.IOException;

import cn.mibcxb.android.os.Logger;

public class McFileUtils {
    private static final Logger LOGGER = Logger.createLogger(McFileUtils.class);

    private McFileUtils() {
    }

    public static boolean createDiretory(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.exists() && dir.isDirectory()) {
            return true;
        }
        return dir.mkdirs();
    }

    public static boolean createFile(File file) throws IOException {
        if (file == null) {
            return false;
        }
        if (file.exists() && file.isFile()) {
            LOGGER.d("Already exists: " + file.getAbsolutePath());
            return false;
        }
        if (createDiretory(file.getParentFile())) {
            return file.createNewFile();
        } else {
            LOGGER.d("Create parent failed: " + file.getParent());
            return false;
        }
    }
}
