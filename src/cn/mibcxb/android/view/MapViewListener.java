package cn.mibcxb.android.view;

import cn.mibcxb.android.map.GeoPoint;

public interface MapViewListener {
    void onMove(GeoPoint point);

    void onZoom(int zoom);
}
