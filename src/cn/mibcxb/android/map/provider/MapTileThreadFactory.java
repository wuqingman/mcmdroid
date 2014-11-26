package cn.mibcxb.android.map.provider;

import java.util.concurrent.ThreadFactory;

import android.text.TextUtils;

class MapTileThreadFactory implements ThreadFactory {
    private final String name;

    public MapTileThreadFactory(final String name) {
        if (TextUtils.isEmpty(name)) {
            this.name = "MapTileThread";
        } else {
            this.name = name;
        }
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }
}
