package cn.mibcxb.android.map.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.cache.DatabaseTileCache;
import cn.mibcxb.android.os.Logger;

public abstract class OnlineTileSource extends TileSource {
    private static final Logger LOGGER = Logger
            .createLogger(OnlineTileSource.class);

    protected final String[] urls;
    protected final Random random = new Random();
    protected final DatabaseTileCache cache;

    public OnlineTileSource(Context context, String name, int minZoomLevel,
            int maxZoomLevel, String... urls) {
        super(context, name, minZoomLevel, maxZoomLevel);
        this.urls = urls;
        this.cache = new DatabaseTileCache(name);
    }

    @Override
    public Drawable getDrawable(MapTile tile) {
        Drawable drawable = getLocalTile(tile);
        if (null == drawable) {
            drawable = getOnlineTile(tile);
        }
        return drawable;
    }

    protected String getBaseUrl() {
        if (null != urls && urls.length > 0) {
            return urls[random.nextInt(urls.length)];
        }
        return null;
    }

    protected void clearCache() {
        cache.clear();
    }

    protected Drawable getOnlineTile(MapTile tile) {
        if (null == tile) {
            return null;
        }

        Bitmap bitmap = null;
        byte[] data = cache.get(tile);
        if (data != null) {
            bitmap = decode(data);
            if (bitmap != null) {
                LOGGER.d("Load from cache: " + tile.toString());
                return getDrawable(bitmap);
            }
        }

        String tileUrl = getTileUrl(tile);
        if (TextUtils.isEmpty(tileUrl)) {
            return null;
        }

        InputStream is = null;
        try {
            URL url = new URL(tileUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(15 * 1000);
            connection.setReadTimeout(10 * 1000);
            connection.setRequestMethod("GET");

            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = connection.getInputStream();
                bitmap = decode(is);
                if (bitmap != null) {
                    LOGGER.d("Load from internet: " + tile.toString());
                    cache.put(tile, bitmap);
                }
            }
        } catch (MalformedURLException e) {
            LOGGER.e(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.e(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return getDrawable(bitmap);
    }

    protected abstract String getTileUrl(MapTile tile);

    protected abstract Drawable getLocalTile(MapTile tile);

}
