package cn.mibcxb.android.map.cache;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.text.TextUtils;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.database.DatabaseManager;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.util.McIOUtils;

public class DatabaseCache implements ExtTileCache {
    private static final Logger LOGGER = Logger
            .createLogger(DatabaseCache.class);

    private static final String TABLE = "tiles";
    private static final String SELECTION = "z=? AND x=? AND y=?";

    private final String path;

    public DatabaseCache(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException(
                    "The database file path cannot be EMPTY.");
        }
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Bitmap read(MapTile tile) {
        if (tile == null) {
            return null;
        }

        DatabaseManager dm = DatabaseManager.createInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(path);
            if (database != null) {
                String[] selectionArgs = new String[] {
                        Integer.toString(tile.getZ()),
                        Integer.toString(tile.getX()),
                        Integer.toString(tile.getY()) };
                Cursor cursor = database.query(TABLE, null, SELECTION,
                        selectionArgs, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(cursor
                            .getColumnIndex(TileColumns.DATA));
                    if (data != null && data.length > 0) {
                        return BitmapFactory.decodeByteArray(data, 0,
                                data.length);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean write(MapTile tile, Bitmap bitmap) {
        if (tile == null || bitmap == null) {
            return false;
        }

        DatabaseManager dm = DatabaseManager.createInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(path);
            if (database != null) {
                synchronized (database) {
                    ContentValues values = createContentValues(tile, bitmap);
                    if (values != null) {
                        long id = -1;
                        try {
                            database.beginTransaction();
                            id = database.insertOrThrow(TABLE, null, values);
                            database.setTransactionSuccessful();
                        } catch (SQLiteException e) {
                            LOGGER.w(e.getMessage(), e);
                        } finally {
                            database.endTransaction();
                        }
                        return id != -1;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean delete(MapTile tile) {
        if (tile == null) {
            return false;
        }

        DatabaseManager dm = DatabaseManager.createInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(path);
            if (database != null) {
                synchronized (database) {
                    String[] selectionArgs = new String[] {
                            Integer.toString(tile.getZ()),
                            Integer.toString(tile.getX()),
                            Integer.toString(tile.getY()) };
                    int count = 0;
                    try {
                        database.beginTransaction();
                        count = database
                                .delete(TABLE, SELECTION, selectionArgs);
                        database.setTransactionSuccessful();
                    } catch (SQLiteException e) {
                        LOGGER.w(e.getMessage(), e);
                    } finally {
                        database.endTransaction();
                    }
                    return count != 0;
                }
            }
        }
        return false;
    }

    @Override
    public void clear() {
        DatabaseManager dm = DatabaseManager.createInstance();
        if (dm != null) {
            SQLiteDatabase database = dm.openOrCreateDatabase(path);
            if (database != null) {
                synchronized (database) {
                    try {
                        database.beginTransaction();
                        database.delete(TABLE, null, null);
                        database.setTransactionSuccessful();
                    } catch (SQLiteException e) {
                        LOGGER.w(e.getMessage(), e);
                    } finally {
                        database.endTransaction();
                    }
                }
            }
        }
    }

    @Override
    public void trim() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        DatabaseManager dm = DatabaseManager.createInstance();
        if (dm != null) {
            dm.closeDatabase(path);
        }
    }

    private ContentValues createContentValues(MapTile tile, Bitmap bitmap) {
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, baos);
            data = baos.toByteArray();
        } catch (Exception e) {
            LOGGER.w(e.getMessage(), e);
        } finally {
            McIOUtils.closeQuietly(baos);
        }

        if (data != null && data.length > 0) {
            ContentValues values = new ContentValues();
            values.put(TileColumns.Z, tile.getZ());
            values.put(TileColumns.X, tile.getX());
            values.put(TileColumns.Y, tile.getY());
            values.put(TileColumns.TIME, System.currentTimeMillis());
            values.put(TileColumns.DATA, data);
            return values;
        }
        return null;
    }

    public interface TileColumns extends BaseColumns {
        String Z = "z";
        String X = "x";
        String Y = "y";
        String TIME = "time";
        String DATA = "data";
    }
}
