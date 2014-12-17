package cn.mibcxb.android.map.source;

import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;

public abstract class TileSource {
    protected final String name;
    protected final int minZoom;
    protected final int maxZoom;

    protected final Resources resources;

    public TileSource(Context context, String name, int minZoom, int maxZoom) {
        this.name = name;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;

        this.resources = context.getResources();
    }

    protected Bitmap decode(byte[] data) {
        Bitmap bitmap = null;
        if (null != data && data.length > 0) {
            try {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (OutOfMemoryError e) {
                System.gc();
            }
        }
        return bitmap;
    }

    protected Bitmap decode(InputStream is) {
        Bitmap bitmap = null;
        if (null != is) {
            try {
                bitmap = BitmapFactory.decodeStream(is);
            } catch (OutOfMemoryError e) {
                System.gc();
            }
        }
        return bitmap;
    }

    public String getName() {
        return name;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public Resources getResources() {
        return resources;
    }

    public Drawable getDrawable(byte[] data) {
        return getDrawable(decode(data));
    }

    public Drawable getDrawable(Bitmap bitmap) {
        Drawable drawable = null;
        if (null != bitmap) {
            drawable = new BitmapDrawable(resources, bitmap);
        }
        return drawable;
    }

    public void detach() {
    }

    public abstract Drawable getDrawable(MapTile tile);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TileSource other = (TileSource) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TileSource [name=" + name + ", minZoom=" + minZoom
                + ", maxZoom=" + maxZoom + "]";
    }

}
