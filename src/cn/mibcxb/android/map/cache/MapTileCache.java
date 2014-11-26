// Created by plusminus on 17:58:57 - 25.09.2008
package cn.mibcxb.android.map.cache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;

public final class MapTileCache {
    public static final int CACHE_SIZE_DEFAULT = 32;

    private final MemoryTileCache memoryTileCache;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public MapTileCache() {
        this(CACHE_SIZE_DEFAULT);
    }

    public MapTileCache(final int maxCacheSize) {
        this.memoryTileCache = new MemoryTileCache(maxCacheSize);
    }

    public void ensureCapacity(final int capacity) {
        readWriteLock.readLock().lock();
        try {
            memoryTileCache.ensureCapacity(capacity);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public Drawable get(final MapTile tile) {
        readWriteLock.readLock().lock();
        try {
            return this.memoryTileCache.get(tile);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void put(final MapTile tile, final Drawable drawable) {
        if (drawable != null) {
            readWriteLock.writeLock().lock();
            try {
                this.memoryTileCache.put(tile, drawable);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    public boolean contains(final MapTile tile) {
        readWriteLock.readLock().lock();
        try {
            return this.memoryTileCache.containsKey(tile);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void remove(final MapTile tile) {
        memoryTileCache.remove(tile);
    }

    public void clear() {
        readWriteLock.writeLock().lock();
        try {
            this.memoryTileCache.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
