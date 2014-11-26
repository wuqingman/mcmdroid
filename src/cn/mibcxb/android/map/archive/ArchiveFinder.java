package cn.mibcxb.android.map.archive;

import cn.mibcxb.android.map.MapTile;

public abstract class ArchiveFinder {
    public abstract Archive openOrCreateArchive(int z, int x, int y);

    public Archive openOrCreateArchive(MapTile tile) {
        Archive archive = null;
        if (tile != null) {
            archive = openOrCreateArchive(tile.getZ(), tile.getX(), tile.getY());
        }
        return archive;
    }
}
