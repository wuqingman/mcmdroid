package cn.mibcxb.android.map.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import cn.mibcxb.android.util.McFileUtils;

public class DatabaseManager {
    private static final String BASE_PATH = "mcmdroid";

    private static final String SQL_CREATE_TILES = "CREATE TABLE IF NOT EXISTS [tiles] ("
            + "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + "[z] INTEGER NOT NULL, "
            + "[x] INTEGER NOT NULL, "
            + "[y] INTEGER NOT NULL, "
            + "[time] INTEGER NOT NULL DEFAULT (datetime('now','localtime')), "
            + "[data] BLOB);";
    private static final String SQL_CREATE_TILE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS [idx_tile] "
            + "ON [tiles] ([z], [x], [y]);";

    private static DatabaseManager instance;

    public static synchronized DatabaseManager createInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static synchronized DatabaseManager getInstance() {
        return instance;
    }

    public static synchronized void destroyInstance() {
        if (instance != null) {
            for (Entry<String, SQLiteDatabase> entry : instance.databases
                    .entrySet()) {
                SQLiteDatabase database = entry.getValue();
                database.close();
            }
            instance.databases.clear();
        }
        instance = null;
    }

    private final File root;
    private final Map<String, SQLiteDatabase> databases;

    private DatabaseManager() {
        this.root = Environment.getExternalStorageDirectory();
        this.databases = new HashMap<String, SQLiteDatabase>();
    }

    public SQLiteDatabase openOrCreateDatabase(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        SQLiteDatabase database = databases.get(name);
        if (database == null) {
            File file = new File(root, BASE_PATH + File.separator + name);
            try {
                if (file.exists() || McFileUtils.createFile(file)) {
                    database = SQLiteDatabase.openOrCreateDatabase(file, null);
                    database.execSQL(SQL_CREATE_TILES);
                    database.execSQL(SQL_CREATE_TILE_INDEX);
                    databases.put(name, database);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return database;
    }
}
