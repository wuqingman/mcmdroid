package cn.mibcxb.android.map.provider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.cache.MapTileCache;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.view.MapSurface.DrawingHandler;

public abstract class MapTileProvider {
    private static final Logger LOGGER = Logger
            .createLogger(MapTileProvider.class);

    private static final int THREAD_POOL_SIZE_MAX = 8;
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(0x7f00000);

    protected final int id = CURRENT_ID.incrementAndGet();
    protected final MapTileCache tileCache = new MapTileCache();

    protected final String name;
    protected final int threadPoolSize;
    protected final ExecutorService executorService;
    protected final Set<MapTile> working;

    private DrawingHandler handler;

    public MapTileProvider(String name) {
        this.name = name;

        this.threadPoolSize = getThreadPoolSize();
        this.executorService = createExecutorService(threadPoolSize, name);
        this.working = new HashSet<MapTile>(threadPoolSize);
    }

    private int getThreadPoolSize() {
        int coreSize = Runtime.getRuntime().availableProcessors();
        return coreSize < THREAD_POOL_SIZE_MAX ? coreSize
                : THREAD_POOL_SIZE_MAX;
    }

    private ExecutorService createExecutorService(int poolSize, String name) {
        return Executors.newFixedThreadPool(poolSize, new MapTileThreadFactory(
                name));
    }

    public void ensureCapacity(int capacity) {
        tileCache.ensureCapacity(capacity);
    }

    public void detach() {
        executorService.shutdown();
    }

    public DrawingHandler getHandler() {
        return handler;
    }

    public void setHandler(DrawingHandler handler) {
        this.handler = handler;
    }

    public Drawable getMapTile(MapTile mapTile) {
        Drawable drawable = tileCache.get(mapTile);
        if (drawable == null) {
            requestMapTile(mapTile);
        }
        return drawable;
    }

    private void requestMapTile(MapTile mapTile) {
        synchronized (working) {
            if (!working.contains(mapTile) && working.size() < threadPoolSize) {
                MapTileLoader loader = createLoader(mapTile);
                try {
                    executorService.execute(loader);
                    working.add(mapTile);
                } catch (RejectedExecutionException e) {
                    LOGGER.w(e.getMessage(), e);
                }
            }
        }
    }

    private void removeMapTile(MapTile mapTile) {
        synchronized (working) {
            working.remove(mapTile);
        }

        if (handler != null) {
            handler.invalidate();
        }
    }

    protected void loaded(MapTile mapTile, Drawable drawable) {
        tileCache.put(mapTile, drawable);
        removeMapTile(mapTile);
    }

    protected void failed(MapTile mapTile) {
        LOGGER.i("loading tile failed:" + mapTile.toString());
        removeMapTile(mapTile);
    }

    protected abstract MapTileLoader createLoader(MapTile mapTile);

}
