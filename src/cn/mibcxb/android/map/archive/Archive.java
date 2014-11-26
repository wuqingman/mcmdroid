package cn.mibcxb.android.map.archive;

import cn.mibcxb.android.map.MapTile;

public abstract class Archive {
    protected final String path;

    public Archive(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    
    public abstract boolean create();

    public abstract byte[] get(MapTile tile);

    public abstract boolean put(MapTile tile, byte[] data);

    public abstract boolean delete(MapTile tile);

    public abstract void trim();

    public abstract void clear();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        Archive other = (Archive) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Archive [path=" + path + "]";
    }
}
