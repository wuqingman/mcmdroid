package cn.mibcxb.android.view.overlay;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import cn.mibcxb.android.map.GeoPoint;
import cn.mibcxb.android.map.Viewport;

public class ScaleBarOverlay extends Overlay {
    public enum Units {
        SI, IMPERIAL, NAUTICAL
    }

    public static final int ALIGN_LEFT = 1 << 0;
    public static final int ALIGN_RIGHT = 1 << 1;
    public static final int ALIGN_TOP = 1 << 2;
    public static final int ALIGN_BOTTOM = 1 << 3;

    private int align = ALIGN_LEFT | ALIGN_BOTTOM;
    private int offsetX = 0;
    private int offsetY = 0;

    private int textColor = Color.BLACK;
    private float textSize = getDefaultTextSize();
    private int lineColor = Color.BLACK;
    private int lineWidth = 3;
    private float length = 2.54f;
    private Units units = Units.SI;
    private Point point;
    private boolean reset = false;

    private final Paint textPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Rect textBounds = new Rect();
    private final Path linePath = new Path();

    public ScaleBarOverlay(Context context) {
        super(context);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Style.FILL_AND_STROKE);

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Style.STROKE);
    }

    @Override
    protected void drawOverlay(Canvas canvas, Viewport projection) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float scaleBarLength = calcScaleBarLength();
        if (point == null || reset) {
            point = createPoint(width, height, scaleBarLength);
            reset = false;
        }

        String text = getScaleBarText(projection, scaleBarLength);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float x = (point.x + scaleBarLength / 2f) - (textBounds.width() >> 1);
        float y = point.y - textBounds.height() * 0.25f;
        canvas.drawText(text, x, y, textPaint);

        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);
        linePath.rewind();
        linePath.moveTo(point.x, point.y - textBounds.height() * 0.5f);
        linePath.lineTo(point.x, point.y);
        linePath.lineTo(point.x + scaleBarLength, point.y);
        linePath.lineTo(point.x + scaleBarLength, point.y - textBounds.height()
                * 0.5f);
        canvas.drawPath(linePath, linePaint);
    }

    private Point createPoint(int width, int height, float scaleBarLength) {
        float x = 0;
        float y = 0;
        if ((align & ALIGN_LEFT) != 0) {
            x = offsetX;
        } else if ((align & ALIGN_RIGHT) != 0) {
            x = width - offsetX - scaleBarLength;
        } else {
            x = (width - scaleBarLength) / 2;
        }

        if ((align & ALIGN_TOP) != 0) {
            y = offsetY;
        } else if ((align & ALIGN_BOTTOM) != 0) {
            y = height - offsetY;
        } else {
            y = height / 2f;
        }
        return new Point((int) x, (int) y);
    }

    private float calcScaleBarLength() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        switch (units) {
        case SI:
            return metrics.xdpi * length / 2.54f;
        default:
            return metrics.xdpi * length;
        }
    }

    private String getScaleBarText(Viewport projection, float scaleBarLength) {
        GeoPoint geoPoint = projection.canvas2GeoPoint(point.x, point.y);
        double resolution = projection.getMercator().calcGroundResolution(
                geoPoint.getLatitude(), projection.getZoom());
        double distance = resolution * scaleBarLength;
        switch (units) {
        case IMPERIAL:
            return "TODO";
        default:
            if (distance > 5000) {
                return String
                        .format(Locale.US, "%dKM", (int) (distance / 1000));
            } else if (distance >= 100) {
                return String.format(Locale.US, "%.2fKM", (distance / 1000));
            } else {
                return String.format(Locale.US, "%.2fM", (distance / 1000));
            }
        }
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
        this.reset = true;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
        this.reset = true;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        this.reset = true;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
        this.reset = true;
    }

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units;
        this.reset = true;
    }
}
