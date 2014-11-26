package cn.mibcxb.android.map.provider;

import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.source.TileSource;
import cn.mibcxb.android.os.Logger;

public class DefaultMapTileProvider extends MapTileProvider {
    private static final Logger LOGGER = Logger
            .createLogger(DefaultMapTileProvider.class);

    private final TileSource source;

    public DefaultMapTileProvider(String name, TileSource source) {
        super(name);
        this.source = source;
    }

    @Override
    protected MapTileLoader createLoader(MapTile mapTile) {
        return new DefaultMapTileLoader(this, mapTile);
    }

    class DefaultMapTileLoader extends MapTileLoader {

        public DefaultMapTileLoader(MapTileProvider provider, MapTile mapTile) {
            super(provider, mapTile);
        }

        @Override
        protected Drawable loadMapTile(MapTile mapTile) {
            Drawable drawable = null;
            if (source != null) {
                try {
                    drawable = source.getDrawable(mapTile);
                } catch (Exception e) {
                    LOGGER.e(e.getMessage(), e);
                }
            }
            return drawable;
        }

    }

}
