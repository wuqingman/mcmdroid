package cn.mibcxb.android.map.provider;

import cn.mibcxb.android.map.MapTile;
import android.graphics.drawable.Drawable;

public abstract class MapTileLoader implements Runnable {

    protected final MapTileProvider provider;
    protected final MapTile mapTile;

    public MapTileLoader(MapTileProvider provider, MapTile mapTile) {
        this.provider = provider;
        this.mapTile = mapTile;
    }

    @Override
    public void run() {
        if (provider != null) {
            Drawable drawable = loadMapTile(mapTile);
            if (drawable != null) {
                provider.loaded(mapTile, drawable);
            } else {
                provider.failed(mapTile);
            }
        }
    }

    protected abstract Drawable loadMapTile(MapTile mapTile);
}
