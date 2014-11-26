package cn.mibcxb.android.map;

import android.graphics.Point;
import android.graphics.Rect;
import cn.mibcxb.android.map.projection.Mercator;
import cn.mibcxb.android.map.projection.MercatorCoordinate;

public final class Viewport {
    private final int zoom;
    private final Rect bounds;
    private final int width;
    private final int height;
    private final Mercator mercator;

    public Viewport(int zoom, Rect bounds, int width, int height,
            Mercator mercator) {
        this.zoom = zoom;
        this.bounds = bounds;
        this.width = width;
        this.height = height;
        this.mercator = mercator;
    }

    public int getZoom() {
        return zoom;
    }

    public Rect getBounds() {
        return bounds;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Mercator getMercator() {
        return mercator;
    }


    public Point coordinate2Canvas(MercatorCoordinate coordinate, Point reuse) {
        Point out = reuse != null ? reuse : new Point();
        out.x = coordinate.getX() - bounds.left;
        out.y = coordinate.getY() - bounds.top;
        return out;
    }

    public MercatorCoordinate canvas2Coordinate(Point point,
            MercatorCoordinate reuse) {
        MercatorCoordinate out = reuse != null ? reuse : mercator
                .obtainMercatorCoordinate();
        out.setZoom(zoom);
        out.set(point.x + bounds.left, point.y + bounds.top);
        return out;
    }

    public Point geoPoint2Canvas(GeoPoint geoPoint, Point reuse) {
        return coordinate2Canvas(
                mercator.obtainMercatorCoordinate(zoom, geoPoint), reuse);
    }

    public GeoPoint canvas2GeoPoint(Point point) {
        return canvas2Coordinate(point, null).getGeoPoint();
    }

    public GeoPoint canvas2GeoPoint(int x, int y) {
        double latitude = mercator.coordinateY2Latitude(y + bounds.top, zoom);
        double longitude = mercator.coordinateX2Longitude(x + bounds.left,
                zoom);
        return new GeoPoint(latitude, longitude);
    }
}
