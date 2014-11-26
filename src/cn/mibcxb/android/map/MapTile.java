/**
 * 
 */
package cn.mibcxb.android.map;

import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mibcxb
 *
 */
public class MapTile implements Parcelable {
    private final int z;
    private final int x;
    private final int y;

    public MapTile(int z, int x, int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String format() {
        return String.format(Locale.US, "%d-%d-%d", z, x, y);
    }

    @Override
    public String toString() {
        return "MapTile [z=" + z + ", x=" + x + ", y=" + y + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
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
        MapTile other = (MapTile) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(z);
        dest.writeInt(x);
        dest.writeInt(y);
    }

    public static final Parcelable.Creator<MapTile> CREATOR = new Parcelable.Creator<MapTile>() {

        @Override
        public MapTile createFromParcel(Parcel source) {
            int z = source.readInt();
            int x = source.readInt();
            int y = source.readInt();
            return new MapTile(z, x, y);
        }

        @Override
        public MapTile[] newArray(int size) {
            return new MapTile[size];
        }
    };
}
