package cn.mibcxb.android.view.overlay.item;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import cn.mibcxb.android.map.Viewport;

public class LineItem extends AbsItem {
    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final List<PointItem> pointItemList = new ArrayList<PointItem>();

    private boolean showPointItem = true;

    private int strokeColor = Color.RED;
    private float strokeWidth = 2f;

    public LineItem(Context context) {
        paint.setAntiAlias(true);
        paint.setColor(strokeColor);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
    }

    public boolean appendPointItem(PointItem item) {
        if (item != null) {
            return pointItemList.add(item);
        }
        return false;
    }

    public boolean removePointItem(PointItem item) {
        return pointItemList.remove(item);
    }

    public void clearPointItem() {
        pointItemList.clear();
    }

    public boolean isShowPointItem() {
        return showPointItem;
    }

    public void setShowPointItem(boolean showPointItem) {
        this.showPointItem = showPointItem;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    protected void layout(Viewport projection) {
        for (PointItem item : pointItemList) {
            item.layout(projection);
        }
    }

    @Override
    protected void draw(Canvas canvas) {
        path.rewind();
        for (int i = 0; i < pointItemList.size(); i++) {
            PointItem item = pointItemList.get(i);
            if (i == 0) {
                path.moveTo(item.getPoint().x, item.getPoint().y);
            } else {
                path.lineTo(item.getPoint().x, item.getPoint().y);
            }
        }
        paint.setColor(strokeColor);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawPath(path, paint);

        if (showPointItem) {
            for (PointItem item : pointItemList) {
                item.draw(canvas);
            }
        }
    }

    @Override
    protected boolean contains(int x, int y) {
        return false;
    }

}
