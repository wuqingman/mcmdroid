package cn.mibcxb.android.map.projection;

import cn.mibcxb.android.map.GeoPoint;

public class MercatorCoordinate {
    private final Mercator mercator;
    private int zoom;
    private int x;
    private int y;

    MercatorCoordinate(Mercator mercator) {
        this.mercator = mercator;
        this.zoom = 0;
        this.x = 0;
        this.y = 0;
    }

    public Mercator getMercator() {
        return mercator;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        int target = mercator.clipZoom(zoom);
        if (target != this.zoom) {
            int diff = Math.abs(target - this.zoom);
            if (target > this.zoom) {
                this.x = this.x << diff;
                this.y = this.y << diff;
            } else {
                this.x = this.x >> diff;
                this.y = this.y >> diff;
            }
            this.zoom = target;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int x, int y) {
        this.x = mercator.clipCoordinate(zoom, x);
        this.y = mercator.clipCoordinate(zoom, y);
    }

    public void offset(int dx, int dy) {
        this.x = mercator.clipCoordinate(zoom, this.x + dx);
        this.y = mercator.clipCoordinate(zoom, this.y + dy);
    }

    public void set(GeoPoint gp) {
        if (gp == null) {
            throw new IllegalArgumentException("The GeoPoint cannot be empty.");
        }

        this.x = mercator.longitude2CoordinateX(gp.getLongitude(), zoom);
        this.y = mercator.latitude2CoordinateY(gp.getLatitude(), zoom);
    }

    public GeoPoint getGeoPoint() {
        double latitude = mercator.coordinateY2Latitude(y, zoom);
        double longitude = mercator.coordinateX2Longitude(x, zoom);
        return new GeoPoint(latitude, longitude);
    }
}
