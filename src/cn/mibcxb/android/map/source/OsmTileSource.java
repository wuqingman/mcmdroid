/**
 * 
 */
package cn.mibcxb.android.map.source;

import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;

/**
 * @author mibcxb
 *
 */
public class OsmTileSource extends OnlineTileSource {
    private static final String[] OPEN_STREET_MAP_URLS = {
            "http://a.tile.openstreetmap.org/%d/%d/%d.png",
            "http://b.tile.openstreetmap.org/%d/%d/%d.png",
            "http://c.tile.openstreetmap.org/%d/%d/%d.png" };

    /**
     * @param context
     * @param guid
     * @param name
     * @param minZoomLevel
     * @param maxZoomLevel
     * @param urls
     */
    public OsmTileSource(Context context, String name, int minZoomLevel,
            int maxZoomLevel) {
        super(context, name, minZoomLevel, maxZoomLevel, OPEN_STREET_MAP_URLS);
    }

    @Override
    protected String getTileUrl(MapTile tile) {
        return String.format(Locale.US, getBaseUrl(), tile.getZ(), tile.getX(),
                tile.getY());
    }

    @Override
    protected Drawable getLocalTile(MapTile tile) {
        // TODO Auto-generated method stub
        return null;
    }

}
