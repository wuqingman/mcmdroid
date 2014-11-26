package cn.mibcxb.android.map;

import java.io.Serializable;

public final class Rect implements Serializable {
    private static final long serialVersionUID = 5033943450739457776L;

    public int left;
    public int top;
    public int right;
    public int bottom;

    public Rect() {
    }

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Rect(Rect r) {
        if (r == null) {
            this.left = this.top = this.right = this.bottom = 0;
        } else {
            this.left = r.left;
            this.top = r.top;
            this.right = r.right;
            this.bottom = r.bottom;
        }
    }

    public final boolean isEmpty() {
        return left >= right || top >= bottom;
    }

    public void setEmpty() {
        left = right = top = bottom = 0;
    }

    public final int width() {
        return right - left;
    }

    public final int height() {
        return bottom - top;
    }

    public final int centerX() {
        return (left + right) >> 1;
    }

    public final int centerY() {
        return (top + bottom) >> 1;
    }

    public final float exactCenterX() {
        return (left + right) * 0.5f;
    }

    public final float exactCenterY() {
        return (top + bottom) * 0.5f;
    }

    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(Rect r) {
        if (r == null) {
            this.left = this.top = this.right = this.bottom = 0;
        } else {
            this.left = r.left;
            this.top = r.top;
            this.right = r.right;
            this.bottom = r.bottom;
        }
    }

    public void offset(int dx, int dy) {
        left += dx;
        top += dy;
        right += dx;
        bottom += dy;
    }

    public void inset(int dx, int dy) {
        left += dx;
        top += dy;
        right -= dx;
        bottom -= dy;
    }

    public boolean contains(int x, int y) {
        return left < right && top < bottom // check for empty first
                && x >= left && x < right && y >= top && y < bottom;
    }

    public boolean contains(int left, int top, int right, int bottom) {
        // check for empty first
        return this.left < this.right && this.top < this.bottom
                // now check for containment
                && this.left <= left && this.top <= top && this.right >= right
                && this.bottom >= bottom;
    }

    public boolean contains(Rect r) {
        // check for empty first
        return this.left < this.right && this.top < this.bottom
                // now check for containment
                && left <= r.left && top <= r.top && right >= r.right
                && bottom >= r.bottom;
    }

    public boolean intersect(int left, int top, int right, int bottom) {
        if (this.left < right && left < this.right && this.top < bottom
                && top < this.bottom) {
            if (this.left < left)
                this.left = left;
            if (this.top < top)
                this.top = top;
            if (this.right > right)
                this.right = right;
            if (this.bottom > bottom)
                this.bottom = bottom;
            return true;
        }
        return false;
    }

    public boolean intersect(Rect r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    public boolean intersects(int left, int top, int right, int bottom) {
        return this.left < right && left < this.right && this.top < bottom
                && top < this.bottom;
    }

    public static boolean intersects(Rect a, Rect b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom
                && b.top < a.bottom;
    }

    public void union(int left, int top, int right, int bottom) {
        if ((left < right) && (top < bottom)) {
            if ((this.left < this.right) && (this.top < this.bottom)) {
                if (this.left > left)
                    this.left = left;
                if (this.top > top)
                    this.top = top;
                if (this.right < right)
                    this.right = right;
                if (this.bottom < bottom)
                    this.bottom = bottom;
            } else {
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
            }
        }
    }

    public void union(Rect r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    public void union(int x, int y) {
        if (x < left) {
            left = x;
        } else if (x > right) {
            right = x;
        }
        if (y < top) {
            top = y;
        } else if (y > bottom) {
            bottom = y;
        }
    }

    public void sort() {
        if (left > right) {
            int temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            int temp = top;
            top = bottom;
            bottom = temp;
        }
    }

    public void scale(float scale) {
        if (scale != 1.0f) {
            left = (int) (left * scale + 0.5f);
            top = (int) (top * scale + 0.5f);
            right = (int) (right * scale + 0.5f);
            bottom = (int) (bottom * scale + 0.5f);
        }
    }
}
