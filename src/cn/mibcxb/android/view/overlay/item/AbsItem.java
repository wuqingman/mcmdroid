package cn.mibcxb.android.view.overlay.item;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Canvas;
import android.view.View;
import cn.mibcxb.android.map.Viewport;

public abstract class AbsItem {
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(0);

    public static final int VISIBLE = View.VISIBLE;
    public static final int INVISIBLE = View.INVISIBLE;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_FOCUS = 1;
    public static final int STATE_SELECT = 2;

    private final int id = CURRENT_ID.incrementAndGet();

    private int visibility = VISIBLE;
    private int state = STATE_NORMAL;

    public AbsItem() {
    }

    public int getId() {
        return id;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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
        AbsItem other = (AbsItem) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AbsItem [id=" + id + ", visibility=" + visibility + ", state="
                + state + "]";
    }

    public final void draw(Canvas canvas, Viewport projection) {
        if (visibility == VISIBLE) {
            layout(projection);
            draw(canvas);
        }
    }

    protected abstract void layout(Viewport projection);

    protected abstract void draw(Canvas canvas);

    protected abstract boolean contains(int x, int y);

}
