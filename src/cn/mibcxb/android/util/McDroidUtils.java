package cn.mibcxb.android.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class McDroidUtils {
    public static final float getNormalTextSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
        case DisplayMetrics.DENSITY_HIGH:
            return 18;
        case DisplayMetrics.DENSITY_XHIGH:
            return 24;
        case DisplayMetrics.DENSITY_XXHIGH:
            return 32;
        case DisplayMetrics.DENSITY_XXXHIGH:
            return 48;
        default:
            return 12;
        }
    }
}
