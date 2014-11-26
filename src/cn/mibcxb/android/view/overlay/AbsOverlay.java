package cn.mibcxb.android.view.overlay;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import cn.mibcxb.android.McMath;
import cn.mibcxb.android.map.R;
import cn.mibcxb.android.map.Viewport;

public abstract class AbsOverlay {
    public static final int ALPHA_MIN = 0x00;
    public static final int ALPHA_MAX = 0xff;

    private final int id;
    private final Resources resources;

    private boolean enabled = true;
    private int alpha = ALPHA_MAX;

    public AbsOverlay(Context context, int id) {
        this.id = id;
        this.resources = context.getResources();
    }

    public int getId() {
        return id;
    }

    public Resources getResources() {
        return resources;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = McMath.clip(alpha, ALPHA_MIN, ALPHA_MAX);
    }

    public boolean canDraw() {
        return enabled && alpha > ALPHA_MIN;
    }

    protected void detach() {
    }

    protected float getDefaultTextSize() {
        return resources
                .getDimensionPixelSize(R.dimen.overlay_text_size_normal);
    }

    protected boolean onClick(Viewport projection, MotionEvent e) {
        return false;
    }

    protected boolean onDoubleClick(Viewport projection, MotionEvent e) {
        return false;
    }

    protected boolean onLongPress(Viewport projection, MotionEvent e) {
        return false;
    }

    protected boolean onScroll(Viewport projection, MotionEvent e1,
            MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    protected boolean onFling(Viewport projection, MotionEvent e1,
            MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbsOverlay other = (AbsOverlay) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
