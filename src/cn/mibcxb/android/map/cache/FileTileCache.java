package cn.mibcxb.android.map.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.util.McFileUtils;
import cn.mibcxb.android.util.McIOUtils;

public class FileTileCache implements ExtTileCache {
    private static final Logger LOGGER = Logger
            .createLogger(FileTileCache.class);

    private final String path;

    public FileTileCache(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException(
                    "The database file path cannot be EMPTY.");
        }
        this.path = path;
    }

    @Override
    public Bitmap read(MapTile tile) {
        if (tile == null) {
            return null;
        }

        File file = new File(getTilePath(tile));
        if (file.exists() && file.isFile()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    @Override
    public boolean write(MapTile tile, Bitmap bitmap) {
        if (tile == null || bitmap == null) {
            return false;
        }

        OutputStream os = null;
        try {
            File file = new File(getTilePath(tile));
            if (McFileUtils.createFile(file)) {
                os = new FileOutputStream(file);
                return bitmap.compress(CompressFormat.PNG, 100, os);
            }
        } catch (IOException e) {
            LOGGER.w(e.getMessage(), e);
        } finally {
            McIOUtils.closeQuietly(os);
        }
        return false;
    }

    @Override
    public boolean delete(MapTile tile) {
        if (tile == null) {
            return false;
        }

        File file = new File(getTilePath(tile));
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public void trim() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    private String getTilePath(MapTile tile) {
        return path
                + String.format(Locale.US, "%d/%d/%d", tile.getZ(),
                        tile.getX(), tile.getY());
    }
}
