package cn.mibcxb.android.view.overlay.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import cn.mibcxb.android.map.GeoPoint;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.map.projection.MercatorCoordinate;

public class PointItem extends AbsItem {
    public static final int DEF_FILL_COLOR = Color.argb(200, 200, 100, 100);
    public static final int DEF_STROKE_COLOR = Color.WHITE;
    public static final int DEF_STROKE_WIDTH = 2;
    public static final int DEF_POINT_RADIUS = 16;

    public static final int GRAVITY_LEFT = 1 << 0;
    public static final int GRAVITY_RIGHT = 1 << 1;
    public static final int GRAVITY_TOP = 1 << 2;
    public static final int GRAVITY_BOTTOM = 1 << 3;

    private MercatorCoordinate coordinate;
    private final GeoPoint geoPoint;

    private final Paint paint = new Paint();
    private final Point point = new Point();
    private final Rect hotspot = new Rect();
    private final SparseArray<Drawable> stateDrawableArray = new SparseArray<Drawable>();

    private int fillColor = DEF_FILL_COLOR;
    private int strokeColor = DEF_STROKE_COLOR;
    private float strokeWidth = DEF_STROKE_WIDTH;
    private int radius = DEF_POINT_RADIUS;
    private int gravity;

    public PointItem(Context context, GeoPoint geoPoint) {
        if (context == null || geoPoint == null) {
            throw new IllegalArgumentException();
        }
        this.geoPoint = geoPoint;

        paint.setAntiAlias(true);
    }

    public MercatorCoordinate getCoordinate() {
        return coordinate;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public Point getPoint() {
        return point;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    protected Drawable getStateDrawable() {
        return stateDrawableArray.get(getState());
    }

    public Drawable getStateDrawable(int state) {
        return stateDrawableArray.get(state);
    }

    public void setStateDrawable(int state, Drawable drawable) {
        if (drawable != null) {
            stateDrawableArray.put(state, drawable);
        }
    }

    @Override
    protected void layout(Viewport projection) {
        if (coordinate == null) {
            coordinate = projection.getMercator().obtainMercatorCoordinate(
                    projection.getZoom(), geoPoint);
        }
        if (coordinate.getZoom() != projection.getZoom()) {
            boolean reset = coordinate.getZoom() < projection.getZoom();
            coordinate.setZoom(projection.getZoom());
            if (reset) {
                coordinate.set(geoPoint);
            }
        }
        projection.coordinate2Canvas(coordinate, point);
    }

    @Override
    protected void draw(Canvas canvas) {
        Drawable drawable = getStateDrawable();
        if (drawable == null) {
            paint.setStyle(Style.FILL);
            paint.setColor(fillColor);
            canvas.drawCircle(point.x, point.y, radius, paint);
            paint.setStyle(Style.STROKE);
            paint.setColor(strokeColor);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawCircle(point.x, point.y, radius, paint);

            hotspot.set(point.x - radius, point.y - radius, point.x + radius,
                    point.y + radius);
        } else {
            int width = drawable.getIntrinsicWidth() >> 1;
            int height = drawable.getIntrinsicHeight() >> 1;

            if ((gravity & GRAVITY_LEFT) != 0) {
                hotspot.left = point.x - width;
                hotspot.right = point.x;
            } else if ((gravity & GRAVITY_LEFT) != 0) {
                hotspot.left = point.x;
                hotspot.right = point.x + width;
            } else {
                hotspot.left = point.x - (width >> 1);
                hotspot.right = point.x + (width >> 1);
            }

            if ((gravity & GRAVITY_TOP) != 0) {
                hotspot.top = point.y - height;
                hotspot.bottom = point.y;
            } else if ((gravity & GRAVITY_BOTTOM) != 0) {
                hotspot.top = point.y;
                hotspot.bottom = point.y + height;
            } else {
                hotspot.top = point.y - (height >> 1);
                hotspot.bottom = point.y + (height >> 1);
            }

            drawable.setBounds(hotspot);
            drawable.draw(canvas);
        }
    }

    @Override
    protected boolean contains(int x, int y) {
        return hotspot.contains(x, y);
    }

}
