package cn.mibcxb.android.map.cache;

import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import cn.mibcxb.android.map.MapTile;

public class MemoryTileCache extends LinkedHashMap<MapTile, Drawable> {

    private static final long serialVersionUID = -541142277575493335L;

    private int capacity;

    public MemoryTileCache(final int capacity) {
        super(capacity + 2, 0.1f, true);
        this.capacity = capacity;
    }

    public void ensureCapacity(final int capacity) {
        if (capacity > this.capacity) {
            this.capacity = capacity;
        }
    }

    @Override
    public Drawable remove(final Object key) {
        final Drawable drawable = super.remove(key);
        // Only recycle if we are running on a project less than 2.3.3
        // Gingerbread
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (drawable instanceof BitmapDrawable) {
                final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
        return drawable;
    }

    @Override
    public void clear() {
        // remove them all individually so that they get recycled
        while (!isEmpty()) {
            remove(keySet().iterator().next());
        }

        // and then clear
        super.clear();
    }

    @Override
    protected boolean removeEldestEntry(
            final java.util.Map.Entry<MapTile, Drawable> aEldest) {
        if (size() > capacity) {
            final MapTile eldest = aEldest.getKey();
            remove(eldest);
            // don't return true because we've already removed it
        }
        return false;
    }

}
