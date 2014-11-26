package cn.mibcxb.android.view.overlay;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.map.projection.Mercator;
import cn.mibcxb.android.map.provider.MapTileProvider;
import cn.mibcxb.android.view.MapSurface.DrawingHandler;

public class TileLayer extends AbsOverlay {
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(
            0x1e000000);

    private final MapTileProvider provider;
    private final Rect tileRect;

    public TileLayer(Context context, MapTileProvider provider) {
        super(context, CURRENT_ID.incrementAndGet());
        this.provider = provider;
        this.tileRect = new Rect();
    }

    public final void draw(Canvas canvas, Viewport projection,
            MapTile[] tiles, Point[] positions) {
        if (canDraw()) {
            drawTileLayer(canvas, projection, tiles, positions);
        }
    }

    public void drawTileLayer(Canvas canvas, Viewport projection,
            MapTile[] tiles, Point[] positions) {
        provider.ensureCapacity(tiles.length + tiles.length >> 1);

        Mercator mercator = projection.getMercator();
        int tileSize = mercator.getTileSize();

        for (int i = 0; i < tiles.length; i++) {
            MapTile tile = tiles[i];
            Point position = positions[i];
            Drawable drawable = provider.getMapTile(tile);

            if (null != drawable) {
                tileRect.set(position.x, position.y, position.x + tileSize,
                        position.y + tileSize);

                drawable.setBounds(tileRect);
                drawable.setAlpha(getAlpha());
                drawable.draw(canvas);
            }
        }
    }

    @Override
    protected void detach() {
        provider.detach();
    }

    public DrawingHandler getHandler() {
        if (provider != null) {
            return provider.getHandler();
        }
        return null;
    }

    public void setHandler(DrawingHandler handler) {
        if (provider != null) {
            provider.setHandler(handler);
        }
    }

}
