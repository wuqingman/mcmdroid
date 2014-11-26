package cn.mibcxb.android.view.overlay.background;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import cn.mibcxb.android.map.R;
import cn.mibcxb.android.map.Viewport;

public class DefaultBackground extends Background {
    private static final String COPYRIGHT = "©2014 mibcxb.cn";

    private final int background = Color.rgb(200, 200, 200);
    private final Paint paint;
    private final Rect bounds;

    public DefaultBackground(Context context) {
        super(context);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xff666666);

        bounds = new Rect();
        paint.setTextSize(getResources().getDimension(
                R.dimen.background_text_size_large));
        paint.getTextBounds(COPYRIGHT, 0, COPYRIGHT.length(), bounds);
    }

    @Override
    public void drawBack(Canvas canvas, Viewport projection) {
        canvas.drawColor(background);

        int x = (canvas.getWidth() - bounds.width()) >> 1;
        int y = (canvas.getHeight() - bounds.height()) >> 1;
        canvas.drawText(COPYRIGHT, x, y, paint);
    }

    @Override
    public BackType getBackType() {
        return BackType.BACK;
    }

}
