package cn.mibcxb.android.map.cache;

import android.graphics.Bitmap;
import cn.mibcxb.android.map.MapTile;

public interface ExtTileCache {
    Bitmap read(MapTile tile);

    boolean write(MapTile tile, Bitmap bitmap);

    boolean delete(MapTile tile);

    void clear();

    void trim();

    void close();
}
