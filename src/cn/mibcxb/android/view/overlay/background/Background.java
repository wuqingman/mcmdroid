package cn.mibcxb.android.view.overlay.background;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.view.overlay.AbsOverlay;

public abstract class Background extends AbsOverlay {
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(
            0x1b000000);

    public Background(Context context) {
        super(context, CURRENT_ID.incrementAndGet());
    }

    public final void draw(Canvas canvas, Viewport projection) {
        if (canDraw()) {
            drawBack(canvas, projection);
        }
    }

    public void drawBack(Canvas canvas, Viewport projection) {
    }

    public final void draw(Canvas canvas, Viewport projection,
            MapTile[] tiles, Point[] positions) {
        if (canDraw()) {
            drawTiles(canvas, projection, tiles, positions);
        }
    }

    public void drawTiles(Canvas canvas, Viewport projection,
            MapTile[] tiles, Point[] positions) {
    }

    public abstract BackType getBackType();

    public enum BackType {
        BACK, TILE;
    }
}
