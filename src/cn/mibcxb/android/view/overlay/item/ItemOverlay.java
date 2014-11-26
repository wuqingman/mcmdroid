package cn.mibcxb.android.view.overlay.item;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.view.overlay.Overlay;

public class ItemOverlay extends Overlay {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_SELECT = 1;

    private final List<AbsItem> itemList = new ArrayList<AbsItem>();
    private int mode = MODE_NORMAL;

    public ItemOverlay(Context context) {
        super(context);
    }

    @Override
    protected void drawOverlay(Canvas canvas, Viewport projection) {
        for (AbsItem item : itemList) {
            item.draw(canvas, projection);
        }
    }

    public boolean add(AbsItem item) {
        if (item != null && !itemList.contains(item)) {
            return itemList.add(item);
        }
        return false;
    }

    public boolean remove(AbsItem item) {
        return itemList.remove(item);
    }

    public void clear() {
        itemList.clear();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    protected boolean onClick(Viewport projection, MotionEvent e) {
        if (mode == MODE_NORMAL) {
            int location = -1;
            for (int i = 0; i < itemList.size(); i++) {
                AbsItem item = itemList.get(i);
                item.setState(AbsItem.STATE_NORMAL);
                if (location == -1
                        && item.contains((int) e.getX(), (int) e.getY())) {
                    item.setState(AbsItem.STATE_FOCUS);
                    location = i;
                }
            }
            return location != -1;
        } else {
            int count = 0;
            for (AbsItem item : itemList) {
                if (item.contains((int) e.getX(), (int) e.getY())) {
                    item.setState(AbsItem.STATE_SELECT);
                    count++;
                }
            }
            return count > 0;
        }
    }

    @Override
    protected boolean onLongPress(Viewport projection, MotionEvent e) {
//        for (AbsItem item : itemList) {
//            if (item.contains((int) e.getX(), (int) e.getY())) {
//                return true;
//            }
//        }
//        mode = MODE_SELECT;
//        return true;
        return false;
    }
}
