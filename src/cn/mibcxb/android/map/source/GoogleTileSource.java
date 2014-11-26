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
public class GoogleTileSource extends OnlineTileSource {

    private static final String[] GOOGLE_MAP_URLS = {
            "http://mt0.google.cn/vt/lyrs=m@177000000&hl=zh-CN&gl=cn&src=app&z=%d&x=%d&y=%d",
            "http://mt1.google.cn/vt/lyrs=m@177000000&hl=zh-CN&gl=cn&src=app&z=%d&x=%d&y=%d",
            "http://mt2.google.cn/vt/lyrs=m@177000000&hl=zh-CN&gl=cn&src=app&z=%d&x=%d&y=%d",
            "http://mt3.google.cn/vt/lyrs=m@177000000&hl=zh-CN&gl=cn&src=app&z=%d&x=%d&y=%d" };

    public GoogleTileSource(Context context, String name, int minZoomLevel,
            int maxZoomLevel) {
        super(context, name, minZoomLevel, maxZoomLevel, GOOGLE_MAP_URLS);
    }

    @Override
    protected String getTileUrl(MapTile tile) {
        return String.format(Locale.US, getBaseUrl(), tile.getZ(), tile.getX(),
                tile.getY());
    }

    @Override
    protected Drawable getLocalTile(MapTile tile) {
        return null;
    }

}
