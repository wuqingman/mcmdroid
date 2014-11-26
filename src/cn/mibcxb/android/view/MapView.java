package cn.mibcxb.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import cn.mibcxb.android.map.GeoPoint;
import cn.mibcxb.android.map.MapException;
import cn.mibcxb.android.map.database.DatabaseManager;
import cn.mibcxb.android.view.overlay.OverlayManager;

public class MapView extends RelativeLayout implements OnZoomListener {
    private MapSurface surface;
    private ZoomButtonsController zoomButtonsController;
    private boolean zoomButtonsEnabled = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MapView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapView(Context context) {
        super(context);
    }

    public void initialize(Context context, int tileSize) throws MapException {
        zoomButtonsController = new ZoomButtonsController(this);
        zoomButtonsController.setOnZoomListener(this);
        DatabaseManager.createInstance();
        surface = new MapSurface(context, tileSize);
        addView(surface, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (zoomButtonsController != null) {
            zoomButtonsController.setVisible(false);
        }
        surface.getOverlayManager().detach();
        DatabaseManager.destroyInstance();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (zoomButtonsEnabled && zoomButtonsController != null) {
                zoomButtonsController.setVisible(true);
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (zoomButtonsController != null) {
            if (zoomButtonsController.isVisible()
                    && zoomButtonsController.onTouch(this, ev)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            zoomButtonsController.setZoomInEnabled(surface.canZoomIn());
            zoomButtonsController.setZoomOutEnabled(surface.canZoomOut());
        }
    }

    @Override
    public void onZoom(boolean zoomIn) {
        if (zoomIn) {
            surface.zoomIn();
            zoomButtonsController.setZoomInEnabled(surface.canZoomIn());
        } else {
            surface.zoomOut();
            zoomButtonsController.setZoomOutEnabled(surface.canZoomOut());
        }
    }

    public OverlayManager getOverlayManager() {
        return surface.getOverlayManager();
    }

    public MapViewListener getMapViewListener() {
        return surface.getMapViewListener();
    }

    public void setMapViewListener(MapViewListener mapViewListener) {
        surface.setMapViewListener(mapViewListener);
    }

    public boolean isZoomButtonsEnabled() {
        return zoomButtonsEnabled;
    }

    public void setZoomButtonsEnabled(boolean zoomButtonsEnabled) {
        this.zoomButtonsEnabled = zoomButtonsEnabled;
    }

    public int getZoom() {
        return surface.getZoom();
    }

    public void setZoom(int zoom) {
        surface.setZoom(zoom);
    }

    public void setCenter(GeoPoint gp) {
        surface.setCenter(gp);
    }

    public void zoomIn() {
        surface.zoomIn();
    }

    public void zoomOut() {
        surface.zoomOut();
    }

    public boolean canZoomIn() {
        return surface.canZoomIn();
    }

    public boolean canZoomOut() {
        return surface.canZoomOut();
    }

}
