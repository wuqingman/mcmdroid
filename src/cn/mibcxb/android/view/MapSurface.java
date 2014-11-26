package cn.mibcxb.android.view;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import cn.mibcxb.android.McMath;
import cn.mibcxb.android.map.GeoPoint;
import cn.mibcxb.android.map.MapException;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.map.projection.Mercator;
import cn.mibcxb.android.map.projection.MercatorCoordinate;
import cn.mibcxb.android.os.Logger;
import cn.mibcxb.android.os.McHandler;
import cn.mibcxb.android.view.overlay.OverlayManager;

public class MapSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final Logger LOGGER = Logger.createLogger(MapSurface.class);

    private static final long SECOND_IN_MILLIS = 1000;
    private static final long ANIMATION_DURATION = SECOND_IN_MILLIS >> 1;
    private static final double ZOOM_SENSITIVITY = 1.3;
    private static final double ZOOM_LOG_BASE_INV = 1.0 / Math
            .log(2.0 / ZOOM_SENSITIVITY);

    private final GestureDetector gestureDetector;
    private final MultiTouchController<Object> multiTouchController;
    private final OverlayManager overlayManager;
    private final Mercator mercator;
    private final MercatorCoordinate mercatorCenter;
    private final MapSurfaceAnimator animator;
    private final AtomicBoolean animating = new AtomicBoolean(false);
    private final AtomicInteger zoomTarget = new AtomicInteger();
    private final Point scaleCenter = new Point();
    private float scaleFactor = 1.0f;

    private int minZoom = Mercator.ZOOM_MIN;
    private int maxZoom = Mercator.ZOOM_MAX;
    private DrawingThread drawingThread;
    private DrawingBuffer drawingBuffer;
    private final Paint drawingPaint = new Paint();
    private final Object syncObject = new Object();

    private MapViewListener mapViewListener;

    MapSurface(Context context, int tileSize) throws MapException {
        super(context);
        GestureListener listener = new GestureListener();
        gestureDetector = new GestureDetector(context, listener);
        gestureDetector.setOnDoubleTapListener(listener);
        multiTouchController = new MultiTouchController<Object>(
                new MultiTouchCanvas(), false);

        overlayManager = new OverlayManager(context);
        mercator = new Mercator(tileSize);
        mercatorCenter = mercator.obtainMercatorCoordinate();
        mercatorCenter.setZoom(5);

        animator = new MapSurfaceAnimator(ANIMATION_DURATION,
                new DecelerateInterpolator(), new AccelerateInterpolator());

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LOGGER.d("mcmdroid: MapSurface created");
        drawingThread = new DrawingThread(holder);
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        LOGGER.d("mcmdroid: MapSurface changed.");
        createDrawingBuffer(width, height);
        int min = calcMinZoom(width, height);
        if (min > minZoom) {
            minZoom = min;
        }
    }

    private void createDrawingBuffer(int width, int height) {
        if (drawingBuffer == null || drawingBuffer.getWidth() != width
                || drawingBuffer.getHeight() != height) {
            synchronized (syncObject) {
                drawingBuffer = new DrawingBuffer(width, height);
            }
        }
    }

    private int calcMinZoom(int width, int height) {
        int xmin = 0;
        while (mercator.getTileSize() << xmin < width) {
            xmin++;
        }
        int ymin = 0;
        while (mercator.getTileSize() << ymin < height) {
            ymin++;
        }
        return Math.min(xmin, ymin);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LOGGER.d("mcmdroid: MapSurface destroyed.");
        drawingThread.cancel();
        drawingThread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (multiTouchController.onTouchEvent(event)) {
            return true;
        }
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return performClick();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    Viewport getProjection() {
        return new Viewport(mercatorCenter.getZoom(), getViewport(),
                getWidth(), getHeight(), mercator);
    }

    private Rect getViewport() {
        int halfWidth = getWidth() >> 1;
        int halfHeight = getHeight() >> 1;
        return new Rect(mercatorCenter.getX() - halfWidth,
                mercatorCenter.getY() - halfHeight, mercatorCenter.getX()
                        + halfWidth, mercatorCenter.getY() + halfHeight);
    }

    void moveTo(int x, int y) {
        mercatorCenter.set(x, y);
        if (null != mapViewListener) {
            mapViewListener.onMove(mercatorCenter.getGeoPoint());
        }
        if (null != drawingThread) {
            drawingThread.invalidate();
        }
    }

    void moveBy(int dx, int dy) {
        mercatorCenter.offset(dx, dy);
        if (null != mapViewListener) {
            mapViewListener.onMove(mercatorCenter.getGeoPoint());
        }
        if (null != drawingThread) {
            drawingThread.invalidate();
        }
    }

    void fling(int velocityX, int velocityY) {
        if (animating.get()) {
            return;
        } else {
            animating.set(true);
            animator.fling(mercatorCenter.getX(), mercatorCenter.getY(),
                    velocityX, velocityY);
            if (drawingThread != null) {
                drawingThread.invalidate();
            }
        }
    }

    void zoomIn() {
        if (animating.get()) {
            return;
        } else {
            if (canZoomIn()) {
                zoomTarget.set(getZoom() + 1);
                animating.set(true);
                animator.zoomIn();
                scaleCenter.set(getWidth() >> 1, getHeight() >> 1);
                if (drawingThread != null) {
                    drawingThread.invalidate();
                }
            }
        }
    }

    void zoomOut() {
        if (animating.get()) {
            return;
        } else {
            if (canZoomOut()) {
                zoomTarget.set(getZoom() - 1);
                animating.set(true);
                animator.zoomOut();
                scaleCenter.set(getWidth() >> 1, getHeight() >> 1);
                if (drawingThread != null) {
                    drawingThread.invalidate();
                }
            }
        }
    }

    void zoomInAt(int x, int y) {
        if (animating.get()) {
            return;
        } else {
            zoomTarget.set(getZoom() + 1);
            animating.set(true);
            animator.zoomInAt(mercatorCenter.getX(), mercatorCenter.getY(),
                    mercatorCenter.getX() - (getWidth() >> 1) + x,
                    mercatorCenter.getY() - (getHeight() >> 1) + y);
            scaleCenter.set(getWidth() >> 1, getHeight() >> 1);
            if (drawingThread != null) {
                drawingThread.invalidate();
            }
        }
    }

    boolean canZoomIn() {
        return getZoom() < maxZoom;
    }

    boolean canZoomOut() {
        return getZoom() > minZoom;
    }

    int getZoom() {
        return animating.get()
                && animator.getMode() == MapSurfaceAnimator.MODE_SCALE ? zoomTarget
                .get() : mercatorCenter.getZoom();
    }

    void setZoom(int zoom) {
        mercatorCenter.setZoom(zoom);
        if (mapViewListener != null) {
            mapViewListener.onZoom(mercatorCenter.getZoom());
        }
    }

    GeoPoint getCenter() {
        return mercatorCenter.getGeoPoint();
    }

    void setCenter(GeoPoint gp) {
        mercatorCenter.set(gp);
        if (null != mapViewListener) {
            mapViewListener.onMove(mercatorCenter.getGeoPoint());
        }
        if (null != drawingThread) {
            drawingThread.invalidate();
        }
    }

    OverlayManager getOverlayManager() {
        return overlayManager;
    }

    MapViewListener getMapViewListener() {
        return mapViewListener;
    }

    void setMapViewListener(MapViewListener mapViewListener) {
        this.mapViewListener = mapViewListener;
    }

    class GestureListener implements OnGestureListener, OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (overlayManager.onClick(getProjection(), e)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!overlayManager.onClick(getProjection(), e)) {
                zoomInAt((int) e.getX(), (int) e.getY());
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            overlayManager.onLongPress(getProjection(), e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (!overlayManager.onScroll(getProjection(), e1, e2, distanceX,
                    distanceY)) {
                moveBy((int) distanceX, (int) distanceY);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (!overlayManager.onFling(getProjection(), e1, e2, velocityX,
                    velocityY)) {
                fling((int) (-velocityX * .4f), (int) (-velocityY * .4f));
            }
            return true;
        }

    }

    class MultiTouchCanvas implements MultiTouchObjectCanvas<Object> {

        @Override
        public Object getDraggableObjectAtPoint(PointInfo touchPoint) {
            return this;
        }

        @Override
        public void getPositionAndScale(Object obj,
                PositionAndScale objPosAndScaleOut) {
            objPosAndScaleOut.set(0, 0, true, scaleFactor, false, 0, 0, false,
                    0);
        }

        @Override
        public boolean setPositionAndScale(Object obj,
                PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
            float multiTouchScale = newObjPosAndScale.getScale();

            if (multiTouchScale != 1) {
                scaleCenter.set((int) touchPoint.getX(),
                        (int) touchPoint.getY());
                if (multiTouchScale > 1) {
                    if (canZoomIn()) {
                        scaleFactor = multiTouchScale;
                    }
                } else {
                    if (canZoomOut()) {
                        scaleFactor = multiTouchScale;
                    }
                }
            }

            if (null != drawingThread) {
                drawingThread.invalidate();
            }
            return true;
        }

        @Override
        public void selectObject(Object obj, PointInfo touchPoint) {
            // if (scaling.get()) {
            if (animating.get()) {
                return;
            } else {
                if (obj == null && scaleFactor != 1.0f) {
                    final float scaleDiffFloat = (float) (Math.log(scaleFactor) * ZOOM_LOG_BASE_INV);
                    final int scaleDiffInt = Math.round(scaleDiffFloat);
                    if (scaleDiffInt != 0) {
                        final float from = scaleFactor;
                        final float to = (float) Math.pow(2, scaleDiffInt);
                        final int zoom = McMath.clip(mercatorCenter.getZoom()
                                + scaleDiffInt, minZoom, maxZoom);

                        zoomTarget.set(zoom);
                        animating.set(true);
                        animator.scale(from, to);

                        if (null != drawingThread) {
                            drawingThread.invalidate();
                        }
                    } else {
                        scaleFactor = 1.0f;
                        if (null != drawingThread) {
                            drawingThread.invalidate();
                        }
                    }
                }
            }
        }

    }

    class DrawingThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private DrawingHandler drawingHandler;
        private boolean drawing = true;

        DrawingThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        void cancel() {
            drawing = false;
            drawingHandler.cancel();
        }

        @Override
        public void run() {
            Looper.prepare();
            drawingHandler = new DrawingHandler(this);
            overlayManager.setDrawingHandler(drawingHandler);
            drawingHandler.invalidate();
            Looper.loop();
        }

        void draw() {
            if (!drawing) {
                return;
            }

            long timeInMillis = System.currentTimeMillis();

            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null && drawingBuffer != null) {
                if (animating.get()) {
                    int mode = animator.getMode();
                    if (animator.computeTransformation()) {
                        switch (mode) {
                        case MapSurfaceAnimator.MODE_SCROLL:
                        case MapSurfaceAnimator.MODE_FLING:
                            moveTo(animator.getCurrentX(),
                                    animator.getCurrentY());
                            break;
                        case MapSurfaceAnimator.MODE_SCALE:
                            scaleFactor = animator.getCurrentScale();
                            invalidate();
                        default:
                            break;
                        }
                    } else {
                        animating.set(false);
                        if (mode == MapSurfaceAnimator.MODE_SCALE) {
                            if (zoomTarget.get() - mercatorCenter.getZoom() != 0) {
                                setZoom(zoomTarget.get());
                            }
                            scaleFactor = 1.0f;
                        }
                    }
                }

                Viewport projection = getProjection();

                canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                synchronized (syncObject) {
                    if (scaleFactor != 1.0f) {
                        canvas.scale(scaleFactor, scaleFactor, scaleCenter.x,
                                scaleCenter.y);
                    } else {
                        overlayManager.draw(drawingBuffer.getCanvas(),
                                projection);
                    }
                    canvas.drawBitmap(drawingBuffer.getBitmap(), 0, 0,
                            drawingPaint);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            timeInMillis = System.currentTimeMillis() - timeInMillis;
            LOGGER.i("mcmdroid: drawing time = " + timeInMillis);
        }

        void invalidate() {
            drawingHandler.invalidate();
        }

    }

    class DrawingBuffer {
        final Bitmap drawingBitmap;
        final Canvas drawingCanvas;

        public DrawingBuffer(int width, int height) {
            drawingBitmap = Bitmap
                    .createBitmap(width, height, Config.ARGB_8888);
            drawingCanvas = new Canvas(drawingBitmap);
        }

        int getWidth() {
            return drawingBitmap.getWidth();
        }

        int getHeight() {
            return drawingBitmap.getHeight();
        }

        Bitmap getBitmap() {
            return drawingBitmap;
        }

        Canvas getCanvas() {
            return drawingCanvas;
        }
    }

    public static class DrawingHandler extends McHandler<DrawingThread> {
        static final int WHAT_REFRESH_SURFACE = 0x1000;

        public DrawingHandler(DrawingThread r) {
            super(r);
        }

        @Override
        public void handleMessage(DrawingThread r, Message msg) {
            switch (msg.what) {
            case WHAT_REFRESH_SURFACE:
                r.draw();
                break;
            default:
                break;
            }
        }

        public void invalidate() {
            removeMessages(WHAT_REFRESH_SURFACE);
            sendEmptyMessage(WHAT_REFRESH_SURFACE);
        }

        public void cancel() {
            removeMessages(WHAT_REFRESH_SURFACE);
        }
    }
}
