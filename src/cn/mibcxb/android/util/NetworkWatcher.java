package cn.mibcxb.android.util;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkWatcher {
    private static NetworkWatcher instance;

    public static NetworkWatcher createInstance(Context context) {
        if (instance == null) {
            instance = new NetworkWatcher(context);
        }
        return instance;
    }

    public static NetworkWatcher getInstance() {
        return instance;
    }

    public static void destroyInstance(Context context) {
        if (instance != null) {
            instance.destroy(context);
            instance = null;
        }
    }

    private final BroadcastReceiver receiver;
    private final List<NetworkListener> list;
    private NetworkInfo info;

    private NetworkWatcher(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        list = new ArrayList<NetworkListener>();
        receiver = new NetworkStateReceiver();
        context.registerReceiver(receiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void destroy(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        context.unregisterReceiver(receiver);
    }

    public boolean registerListener(NetworkListener listener) {
        if (listener != null && !list.contains(listener)) {
            listener.onNetworkChanged(info);
            return list.add(listener);
        }
        return false;
    }

    public boolean unregisterListener(NetworkListener listener) {
        return list.remove(listener);
    }

    class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                    .getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                info = manager.getActiveNetworkInfo();
                for (NetworkListener listener : list) {
                    listener.onNetworkChanged(info);
                }
            }
        }
    }

    public interface NetworkListener {
        void onNetworkChanged(NetworkInfo info);
    }
}
