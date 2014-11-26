package cn.mibcxb.android.view;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Point;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class MapSurfaceAnimator {
    static final int MODE_NONE = 0;
    static final int MODE_SCALE = 1;
    static final int MODE_SCROLL = 2;
    static final int MODE_FLING = 3;

    private long duration = 500;
    private int mode = MODE_NONE;
    private long startTime = -1;

    private float currentScale;
    private float scaleFrom;
    private float scaleTo;

    private int currentX;
    private int currentY;
    private int startX;
    private int startY;
    private int deltaX;
    private int deltaY;
    private float velocityX;
    private float velocityY;

    private AtomicInteger targetScale;
    private Point targetPoint;

    private final Interpolator moveInterpolator;
    private final Interpolator zoomInterpolator;

    private boolean moveFinished = true;
    private boolean zoomFinished = true;

    MapSurfaceAnimator(long duration, Interpolator moveInterpolator,
            Interpolator zoomInterpolator) {
        this.duration = duration;
        this.moveInterpolator = moveInterpolator;
        this.zoomInterpolator = zoomInterpolator;
    }

    private boolean isAnimating() {
        return !(moveFinished && zoomFinished);
    }

    int getMode() {
        return mode;
    }

    float getCurrentScale() {
        return currentScale;
    }

    int getCurrentX() {
        return currentX;
    }

    int getCurrentY() {
        return currentY;
    }

    void zoomIn() {
        scale(1, 2);
    }

    void zoomOut() {
        scale(1, 0);
    }

    void scale(float from, float to) {
        if (!isAnimating()) {
            zoomFinished = false;
            mode = MODE_SCALE;
            this.scaleFrom = from;
            this.scaleTo = to;
        }
    }

    void scroll(int startX, int startY, int stopX, int stopY) {
        if (!isAnimating()) {
            moveFinished = false;
            mode = MODE_SCROLL;
            this.startX = startX;
            this.startY = startY;
            this.deltaX = stopX - startX;
            this.deltaY = stopY - startY;
        }
    }

    void fling(int startX, int startY, float velocityX, float velocityY) {
        if (!isAnimating()) {
            moveFinished = false;
            mode = MODE_FLING;
            this.startX = startX;
            this.startY = startY;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }
    }

    void zoomInAt(int startX, int startY, int stopX, int stopY) {
        if (!isAnimating()) {
            targetScale = new AtomicInteger(2);
            scroll(startX, startY, stopX, stopY);
        }
    }

    boolean computeTransformation() {
        switch (mode) {
        case MODE_SCALE:
            return computeScaleFactor();
        case MODE_SCROLL:
        case MODE_FLING:
            return computeScrollOffset();
        default:
            return false;
        }
    }

    private boolean computeScaleFactor() {
        if (zoomFinished) {
            mode = MODE_NONE;
            return false;
        }

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        float normalizedTime;

        if (startTime == -1) {
            startTime = currentTime;
        }

        if (duration != 0) {
            normalizedTime = ((float) (currentTime - startTime))
                    / (float) duration;
        } else {
            // time is a step-change with a zero duration
            normalizedTime = currentTime < startTime ? 0.0f : 1.0f;
        }

        float interpolatedTime = zoomInterpolator
                .getInterpolation(normalizedTime) * 0.3f;
        float diff = scaleTo - scaleFrom;
        currentScale = scaleFrom + diff * interpolatedTime;

        if (normalizedTime >= 1.0f) {
            zoomFinished = true;
            startTime = -1;
            if (targetPoint != null) {
                targetPoint = null;
            }
        }
        return true;
    }

    private boolean computeScrollOffset() {
        if (moveFinished) {
            mode = MODE_NONE;
            return false;
        }

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        float normalizedTime;

        if (startTime == -1) {
            startTime = currentTime;
        }

        if (duration != 0) {
            normalizedTime = ((float) (currentTime - startTime))
                    / (float) duration;
        } else {
            // time is a step-change with a zero duration
            normalizedTime = currentTime < startTime ? 0.0f : 1.0f;
        }

        float interpolatedTime = moveInterpolator
                .getInterpolation(normalizedTime);
        if (mode == MODE_FLING) {
            interpolatedTime = interpolatedTime * 0.5f;
            currentX = (int) (startX + velocityX * interpolatedTime);
            currentY = (int) (startY + velocityY * interpolatedTime);
        } else {
            currentX = (int) (startX + deltaX * interpolatedTime);
            currentY = (int) (startY + deltaY * interpolatedTime);
        }

        if (normalizedTime >= 1.0f) {
            moveFinished = true;
            startTime = -1;
            if (targetScale != null) {
                scale(1, targetScale.get());
                targetScale = null;
            }
        }
        return true;
    }
}
