package cn.mibcxb.android.view.overlay;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.map.projection.Mercator;

public class TileInfo extends AbsOverlay {
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(
            0x10000000);
    private static final int DEF_COLOR = 0xff888888;

    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Rect tileRect = new Rect();

    public TileInfo(Context context) {
        super(context, CURRENT_ID.incrementAndGet());

        this.linePaint.setStyle(Style.STROKE);
        this.linePaint.setStrokeWidth(1.0f);
        this.linePaint.setColor(DEF_COLOR);

        this.textPaint.setTextSize(getDefaultTextSize());
        this.textPaint.setAntiAlias(true);
        this.textPaint.setColor(DEF_COLOR);
    }

    public final void draw(Canvas canvas, Viewport projection,
            MapTile[] tiles, Point[] positions) {
        if (!canDraw()) {
            return;
        }

        Mercator mercator = projection.getMercator();
        int tileSize = mercator.getTileSize();

        for (int i = 0; i < tiles.length; i++) {
            MapTile tile = tiles[i];
            Point position = positions[i];
            tileRect.set(position.x, position.y, position.x + tileSize,
                    position.y + tileSize);

            canvas.drawRect(tileRect, linePaint);

            float tx = tileRect.left + 5;
            float ty = tileRect.top + 5;
            canvas.drawText(tile.format(), tx, ty + textPaint.getTextSize(),
                    textPaint);

            double lat = mercator.tileY2Latitude(tile.getY(), tile.getZ());
            double lon = mercator.tileX2Longitude(tile.getX(), tile.getZ());
            canvas.drawText(String.format(Locale.US, "lat: %.6f", lat), tx, ty
                    + textPaint.getTextSize() * 2, textPaint);
            canvas.drawText(String.format(Locale.US, "lng: %.6f", lon), tx, ty
                    + textPaint.getTextSize() * 3, textPaint);
        }
    }

}
