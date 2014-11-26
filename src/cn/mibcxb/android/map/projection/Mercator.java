package cn.mibcxb.android.map.projection;

import java.util.HashSet;
import java.util.Set;

import cn.mibcxb.android.McMath;
import cn.mibcxb.android.map.GeoPoint;
import cn.mibcxb.android.map.MapException;
import cn.mibcxb.android.map.MapTile;

public class Mercator {
    public static final double EARTH_RADIUS = 6378137.0;
    public static final double EARTH_CIRCUMFERENCE = 2 * Math.PI * EARTH_RADIUS;

    public static final double LATITUDE_MAX = 85.05112877980659;
    public static final double LATITUDE_MIN = -LATITUDE_MAX;
    public static final double LATITUDE_SPAN = LATITUDE_MAX - LATITUDE_MIN;
    public static final double LONGITUDE_MAX = 180.0;
    public static final double LONGITUDE_MIN = -LONGITUDE_MAX;
    public static final double LONGITUDE_SPAN = LONGITUDE_MAX - LONGITUDE_MIN;

    public static final int TILE_SIZE = 256;
    public static final int ZOOM_MIN = 0;
    public static final int ZOOM_MAX = 19;

    private final int tileSize;
    private final int minZoom;
    private final int maxZoom;

    public Mercator(int tileSize) throws MapException {
        if (tileSize < TILE_SIZE || tileSize % TILE_SIZE != 0) {
            throw new MapException("Tile size must be a multiple of 256");
        }
        if (tileSize >= (TILE_SIZE << 3)) {
            throw new MapException("Tile size is too large.");
        }

        this.tileSize = tileSize;
        this.minZoom = ZOOM_MIN;
        this.maxZoom = ZOOM_MAX - (tileSize / TILE_SIZE - 1);
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public int calcTileCount(int... zooms) {
        int count = 0;
        if (zooms != null && zooms.length > 0) {
            Set<Integer> zs = new HashSet<Integer>(zooms.length);
            for (int i = 0; i < zooms.length; i++) {
                zs.add(zooms[i]);
            }

            for (Integer z : zs) {
                int n = 1 << z;
                count += n * n;
            }
        }
        return count;
    }

    public int calcMapSize(int zoom) {
        return tileSize << clipZoom(zoom);
    }

    public double calcGroundResolution(double latitude, int zoom) {
        latitude = clipLatitude(latitude);
        return Math.cos(latitude * Math.PI / 180.0) * EARTH_CIRCUMFERENCE
                / calcMapSize(zoom);
    }

    public int clipZoom(int zoom) {
        return McMath.clip(zoom, minZoom, maxZoom);
    }

    public int clipCoordinate(int zoom, int coordinate) {
        int mapSize = calcMapSize(zoom);
        int pixel = coordinate % mapSize;
        if (pixel < 0) {
            pixel += mapSize;
        }
        return pixel;
    }

    public int clipTile(int zoom, int n) {
        int maxValue = 1 << clipZoom(zoom);
        int tile = n % maxValue;
        if (tile < 0) {
            tile += maxValue;
        }
        return tile;
    }

    public static final double clipLatitude(double latitude) {
        return McMath.clip(latitude, LATITUDE_MIN, LATITUDE_MAX);
    }

    public static final double clipLongitude(double longitude) {
        return McMath.clip(longitude, LONGITUDE_MIN, LONGITUDE_MAX);
    }

    public int latitude2CoordinateY(double latitude, int zoom) {
        latitude = clipLatitude(latitude);
        int mapSize = calcMapSize(zoom);

        double sinLatitude = Math.sin(latitude * (Math.PI / 180));
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude))
                / (4 * Math.PI);

        return (int) McMath.clip(y * mapSize, 0, mapSize - 1);
    }

    public int longitude2CoordinateX(double longitude, int zoom) {
        longitude = clipLongitude(longitude);
        int mapSize = calcMapSize(zoom);

        double x = (longitude + 180) / 360;

        return (int) McMath.clip(x * mapSize, 0, mapSize - 1);
    }

    public int latitude2TileY(double latitude, int zoom) {
        int coordinateY = latitude2CoordinateY(latitude, zoom);
        return coordinate2Tile(coordinateY, zoom);
    }

    public int longitude2TileX(double longitude, int zoom) {
        int coordinateX = longitude2CoordinateX(longitude, zoom);
        return coordinate2Tile(coordinateX, zoom);
    }

    public int coordinate2Tile(int coordinate, int zoom) {
        int tile = clipCoordinate(zoom, coordinate) / tileSize;
        return clipTile(zoom, tile);
    }

    public int tile2Coordinate(int tile, int zoom) {
        int coordinate = clipTile(zoom, tile) * tileSize;
        return clipCoordinate(zoom, coordinate);
    }

    public double coordinateY2Latitude(int coordinateY, int zoom) {
        coordinateY = clipCoordinate(zoom, coordinateY);
        double y = 0.5 - (double) coordinateY / calcMapSize(zoom);
        return 90 - 360 * Math.atan(Math.exp(-y * (2 * Math.PI))) / Math.PI;
    }

    public double coordinateX2Longitude(int coordinateX, int zoom) {
        coordinateX = clipCoordinate(zoom, coordinateX);
        return 360 * ((double) coordinateX / calcMapSize(zoom) - 0.5);
    }

    public double tileY2Latitude(int tileY, int zoom) {
        int coordinateY = tile2Coordinate(tileY, zoom);
        return coordinateY2Latitude(coordinateY, zoom);
    }

    public double tileX2Longitude(int tileX, int zoom) {
        int coordinateX = tile2Coordinate(tileX, zoom);
        return coordinateX2Longitude(coordinateX, zoom);
    }

    public MercatorCoordinate obtainMercatorCoordinate() {
        return new MercatorCoordinate(this);
    }

    public MercatorCoordinate obtainMercatorCoordinate(int zoom, GeoPoint point) {
        MercatorCoordinate coordinate = new MercatorCoordinate(this);
        coordinate.setZoom(zoom);
        coordinate.set(point);
        return coordinate;
    }

    public MercatorCoordinate geo2Coordinate(GeoPoint point, int zoom,
            MercatorCoordinate reuse) {
        MercatorCoordinate out = reuse != null ? reuse
                : obtainMercatorCoordinate();
        out.setZoom(zoom);
        out.set(point);
        return out;
    }

    public MapTile getMapTile(GeoPoint point, int zoom) {
        if (point == null) {
            return null;
        }
        return getMapTile(point.getLatitude(), point.getLongitude(), zoom);
    }

    public MapTile getMapTile(double latitude, double longitude, int zoom) {
        zoom = clipZoom(zoom);
        int tileY = latitude2TileY(latitude, zoom);
        int tileX = longitude2TileX(longitude, zoom);
        return new MapTile(zoom, tileX, tileY);
    }

}
