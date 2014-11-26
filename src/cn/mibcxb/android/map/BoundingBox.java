package cn.mibcxb.android.map;

import java.util.HashSet;
import java.util.Set;

import cn.mibcxb.android.map.projection.Mercator;
import android.os.Parcel;
import android.os.Parcelable;

public final class BoundingBox implements Parcelable {
    private final double minLatitude;
    private final double minLongitude;
    private final double maxLatitude;
    private final double maxLongitude;

    private BoundingBox(double minLatitude, double minLongitude,
            double maxLatitude, double maxLongitude) {
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
    }

    private BoundingBox(Parcel source) {
        this.minLatitude = source.readDouble();
        this.minLongitude = source.readDouble();
        this.maxLatitude = source.readDouble();
        this.maxLongitude = source.readDouble();
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maxLatitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxLongitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minLatitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minLongitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(maxLatitude) != Double
                .doubleToLongBits(other.maxLatitude))
            return false;
        if (Double.doubleToLongBits(maxLongitude) != Double
                .doubleToLongBits(other.maxLongitude))
            return false;
        if (Double.doubleToLongBits(minLatitude) != Double
                .doubleToLongBits(other.minLatitude))
            return false;
        if (Double.doubleToLongBits(minLongitude) != Double
                .doubleToLongBits(other.minLongitude))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BoundingBox [minLatitude=" + minLatitude + ", minLongitude="
                + minLongitude + ", maxLatitude=" + maxLatitude
                + ", maxLongitude=" + maxLongitude + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(minLatitude);
        dest.writeDouble(minLongitude);
        dest.writeDouble(maxLatitude);
        dest.writeDouble(maxLongitude);
    }

    public static final Parcelable.Creator<BoundingBox> CREATOR = new Parcelable.Creator<BoundingBox>() {

        @Override
        public BoundingBox createFromParcel(Parcel source) {
            return new BoundingBox(source);
        }

        @Override
        public BoundingBox[] newArray(int size) {
            return new BoundingBox[size];
        }
    };

    public static final BoundingBox from(double minLatitude,
            double minLongitude, double maxLatitude, double maxLongitude)
            throws MapException {
        if (minLatitude >= maxLatitude) {
            throw new MapException(
                    "The minimum latitude must less than the maximum latitude.");
        }
        if (minLongitude >= maxLongitude) {
            throw new MapException(
                    "The minimum longitude must less than the maximum longitude.");
        }
        if (minLatitude < Mercator.LATITUDE_MIN) {
            throw new MapException("The minimum latitude is invalid.");
        }
        if (maxLatitude > Mercator.LATITUDE_MAX) {
            throw new MapException("The maximum latitude is invalid.");
        }
        if (minLongitude < Mercator.LONGITUDE_MIN) {
            throw new MapException("The minimum longitude is invalid.");
        }
        if (maxLongitude > Mercator.LONGITUDE_MAX) {
            throw new MapException("The maximum longitude is invalid.");
        }

        return new BoundingBox(minLatitude, minLongitude, maxLatitude,
                maxLongitude);
    }

    public static final BoundingBox from(GeoPoint... points)
            throws MapException {
        if (points == null || points.length == 0) {
            throw new MapException("The GeoPoint array cannot be empty.");
        }
        Set<GeoPoint> geoPointSet = new HashSet<GeoPoint>();
        for (int i = 0; i < points.length; i++) {
            geoPointSet.add(points[i]);
        }
        if (geoPointSet.size() < 2) {
            throw new MapException(
                    "The GeoPoint array must contain two or more different points.");
        }

        double minLatitude = Mercator.LATITUDE_MAX;
        double minLongitude = Mercator.LONGITUDE_MAX;
        double maxLatitude = Mercator.LATITUDE_MIN;
        double maxLongitude = Mercator.LONGITUDE_MIN;
        for (GeoPoint geoPoint : geoPointSet) {
            minLatitude = Math.min(minLatitude, geoPoint.getLatitude());
            minLongitude = Math.min(minLongitude, geoPoint.getLongitude());
            maxLatitude = Math.max(maxLatitude, geoPoint.getLatitude());
            maxLongitude = Math.max(maxLongitude, geoPoint.getLongitude());
        }
        return from(minLatitude, minLongitude, maxLatitude, maxLongitude);
    }
}
