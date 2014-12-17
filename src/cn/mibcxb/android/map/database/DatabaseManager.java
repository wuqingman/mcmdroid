package cn.mibcxb.android.map.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cn.mibcxb.android.util.McFileUtils;

public class DatabaseManager {
    private static final String TAG = DatabaseManager.class.getSimpleName();

    private static final String SQL_CREATE_TILES = "CREATE TABLE IF NOT EXISTS [tiles] ("
            + "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + "[z] INTEGER NOT NULL, "
            + "[x] INTEGER NOT NULL, "
            + "[y] INTEGER NOT NULL, "
            + "[time] INTEGER NOT NULL DEFAULT (datetime('now','localtime')), "
            + "[data] BLOB NOT NULL);";
    private static final String SQL_CREATE_TILE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS [idx_tile] "
            + "ON [tiles] ([z], [x], [y]);";
    private static final String SQL_CREATE_TIME_INDEX = "CREATE INDEX IF NOT EXISTS [idx_tile] "
            + "ON [tiles] ([time]);";

    private static DatabaseManager instance;

    public static synchronized DatabaseManager createInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static synchronized void destroyInstance() {
        if (instance != null) {
            synchronized (TAG) {
                for (SQLiteDatabase database : instance.databases.values()) {
                    database.close();
                }
                instance.databases.clear();
            }
        }
        instance = null;
    }

    private final Map<String, SQLiteDatabase> databases;

    private DatabaseManager() {
        this.databases = new HashMap<String, SQLiteDatabase>();
    }

    public SQLiteDatabase openOrCreateDatabase(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        SQLiteDatabase database = null;
        synchronized (TAG) {
            database = databases.get(path);
            if (database == null) {
                File file = new File(path);
                try {
                    if (file.exists() || McFileUtils.createFile(file)) {
                        database = SQLiteDatabase.openOrCreateDatabase(file,
                                null);
                        database.execSQL(SQL_CREATE_TILES);
                        database.execSQL(SQL_CREATE_TILE_INDEX);
                        database.execSQL(SQL_CREATE_TIME_INDEX);
                        databases.put(path, database);
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            }
        }
        return database;
    }

    public void closeDatabase(String path) {
        synchronized (TAG) {
            SQLiteDatabase database = databases.remove(path);
            if (database != null) {
                database.close();
            }
        }
    }
}
