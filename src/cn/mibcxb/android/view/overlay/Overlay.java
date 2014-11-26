package cn.mibcxb.android.view.overlay;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Canvas;
import cn.mibcxb.android.map.Viewport;

public abstract class Overlay extends AbsOverlay {
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(
            0x1f000000);

    public Overlay(Context context) {
        super(context, CURRENT_ID.incrementAndGet());
    }

    public final void draw(Canvas canvas, Viewport projection) {
        if (canDraw()) {
            drawOverlay(canvas, projection);
        }
    }

    protected abstract void drawOverlay(Canvas canvas, Viewport projection);
}
