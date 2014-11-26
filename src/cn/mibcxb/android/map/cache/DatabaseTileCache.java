package cn.mibcxb.android.map.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.provider.BaseColumns;
import android.text.TextUtils;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.database.DatabaseManager;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.util.McIOUtils;

public class DatabaseTileCache {
    private static final Logger LOGGER = Logger
            .createLogger(DatabaseTileCache.class);

    private static final String TABLE = "tiles";
    private static final String SELECTION = "z=? AND x=? AND y=?";
    private static final String COL_COUNT = "count('x')";
    private static final String QUERY_COUNT = "SELECT " + COL_COUNT + " FROM "
            + TABLE + " WHERE " + SELECTION;

    private final String fileName;

    public DatabaseTileCache(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("The name cannot be empty.");
        }
        this.fileName = name + File.separator + "tiles.db";
    }

    public byte[] get(MapTile tile) {
        if (tile == null) {
            return null;
        }

        DatabaseManager dm = DatabaseManager.getInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(fileName);
            if (database != null) {
                String[] selectionArgs = new String[] {
                        Integer.toString(tile.getZ()),
                        Integer.toString(tile.getX()),
                        Integer.toString(tile.getY()) };
                Cursor cursor = database.query(TABLE, null, SELECTION,
                        selectionArgs, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        return cursor.getBlob(cursor
                                .getColumnIndex(TileColumns.DATA));
                    }
                }
            }
        }
        return null;
    }

    public void put(MapTile tile, Bitmap bmp) {
        if (tile == null || bmp == null) {
            return;
        }

        DatabaseManager dm = DatabaseManager.getInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(fileName);
            if (database != null) {
                synchronized (database) {
                    ContentValues values = createContentValues(tile, bmp);
                    if (values != null) {
                        try {
                            database.beginTransaction();
                            boolean exists = false;
                            String[] selectionArgs = new String[] {
                                    Integer.toString(tile.getZ()),
                                    Integer.toString(tile.getX()),
                                    Integer.toString(tile.getY()) };
                            Cursor cursor = database.rawQuery(QUERY_COUNT,
                                    selectionArgs);
                            if (cursor != null) {
                                if (cursor.moveToFirst()) {
                                    int index = cursor
                                            .getColumnIndex(COL_COUNT);
                                    if (index != -1) {
                                        exists = cursor.getInt(index) > 0;
                                    }
                                }
                            }

                            if (exists) {
                                database.update(TABLE, values, SELECTION,
                                        selectionArgs);
                            } else {
                                database.insert(TABLE, null, values);
                            }
                            database.setTransactionSuccessful();
                        } catch (Exception e) {
                            LOGGER.e(e.getMessage(), e);
                        } finally {
                            database.endTransaction();
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        DatabaseManager dm = DatabaseManager.getInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(fileName);
            if (database != null) {
                synchronized (database) {
                    try {
                        database.beginTransaction();
                        database.delete(TABLE, null, null);
                        database.setTransactionSuccessful();
                    } catch (Exception e) {
                        LOGGER.e(e.getMessage(), e);
                    } finally {
                        database.endTransaction();
                    }
                }
            }
        }
    }

    private ContentValues createContentValues(MapTile tile, Bitmap bmp) {
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.PNG, 100, baos);
            data = baos.toByteArray();
        } catch (Exception e) {
            LOGGER.e(e.getMessage(), e);
        } finally {
            McIOUtils.closeQuietly(baos);
        }

        if (data != null) {
            ContentValues values = new ContentValues();
            values.put(TileColumns.Z, tile.getZ());
            values.put(TileColumns.X, tile.getX());
            values.put(TileColumns.Y, tile.getY());
            values.put(TileColumns.TIME, System.currentTimeMillis());
            values.put(TileColumns.DATA, data);
            return values;
        } else {
            return null;
        }
    }

    public interface TileColumns extends BaseColumns {
        String Z = "z";
        String X = "x";
        String Y = "y";
        String TIME = "time";
        String DATA = "data";
    }
}
