package cn.mibcxb.android.os;

import cn.mibcxb.android.McMath;
import cn.mibcxb.android.map.BuildConfig;
import android.util.Log;

public final class Logger {
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;

    private final String tag;
    private int level;

    private Logger(String tag) {
        this.tag = tag;
        this.level = BuildConfig.DEBUG ? DEBUG : WARN;
    }

    public String getTag() {
        return tag;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = McMath.clip(level, VERBOSE, ERROR);
    }

    public void v(String msg) {
        if (level <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public void v(String msg, Throwable tr) {
        if (level <= VERBOSE) {
            Log.v(tag, msg, tr);
        }
    }

    public void d(String msg) {
        if (level <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void d(String msg, Throwable tr) {
        if (level <= DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    public void i(String msg) {
        if (level <= INFO) {
            Log.i(tag, msg);
        }
    }

    public void i(String msg, Throwable tr) {
        if (level <= INFO) {
            Log.i(tag, msg, tr);
        }
    }

    public void w(String msg) {
        if (level <= WARN) {
            Log.w(tag, msg);
        }
    }

    public void w(String msg, Throwable tr) {
        if (level <= WARN) {
            Log.w(tag, msg, tr);
        }
    }

    public void e(String msg) {
        if (level <= ERROR) {
            Log.e(tag, msg);
        }
    }

    public void e(String msg, Throwable tr) {
        if (level <= ERROR) {
            Log.e(tag, msg, tr);
        }
    }

    public static final Logger createLogger(Class<?> cls) {
        return new Logger(cls.getSimpleName());
    }
}
