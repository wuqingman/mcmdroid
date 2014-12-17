package cn.mibcxb.android.map.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.text.TextUtils;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.cache.DatabaseCache;
import cn.mibcxb.android.map.cache.ExtTileCache;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.util.NetworkWatcher.NetworkListener;

public abstract class OnlineTileSource extends TileSource implements
        NetworkListener {
    private static final Logger LOGGER = Logger
            .createLogger(OnlineTileSource.class);

    protected final String[] urls;
    protected final Random random = new Random();

    protected File cacheDir;
    protected ExtTileCache cache;
    protected boolean networkEnabled = false;

    public OnlineTileSource(Context context, String name, int minZoomLevel,
            int maxZoomLevel, String... urls) {
        super(context, name, minZoomLevel, maxZoomLevel);
        this.urls = urls;

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null || !cacheDir.exists()) {
            cacheDir = context.getCacheDir();
        }
        this.cacheDir = cacheDir;
        this.cache = createTileCache();
    }

    @Override
    public Drawable getDrawable(MapTile tile) {
        Drawable drawable = getLocalTile(tile);
        if (null == drawable) {
            drawable = getOnlineTile(tile);
        }
        return drawable;
    }

    @Override
    public void detach() {
        if (cache != null) {
            cache.close();
        }
    }

    @Override
    public void onNetworkChanged(NetworkInfo info) {
        if (info != null && info.isAvailable()) {
            networkEnabled = true;
        } else {
            networkEnabled = false;
        }
    }

    protected String getBaseUrl() {
        if (null != urls && urls.length > 0) {
            return urls[random.nextInt(urls.length)];
        }
        return null;
    }

    protected void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    protected Drawable getOnlineTile(MapTile tile) {
        if (null == tile) {
            return null;
        }

        Bitmap bitmap = null;
        if (cache != null) {
            bitmap = cache.read(tile);
            if (bitmap != null) {
                LOGGER.d("Load from cache: " + tile.toString());
                return getDrawable(bitmap);
            }
        }

        if (!networkEnabled) {
            return null;
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
                    if (cache != null && cache.write(tile, bitmap)) {
                        LOGGER.d("Save tile ok: " + tile.toString());
                    }
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

    protected ExtTileCache createTileCache() {
        if (cacheDir != null && cacheDir.exists()) {
            File file = new File(cacheDir, name + File.separator + "tiles.db");
            return new DatabaseCache(file.getAbsolutePath());
        }
        return null;
    }

    protected abstract String getTileUrl(MapTile tile);

    protected abstract Drawable getLocalTile(MapTile tile);

}
