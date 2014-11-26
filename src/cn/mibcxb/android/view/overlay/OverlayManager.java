package cn.mibcxb.android.view.overlay;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import cn.mibcxb.android.map.MapTile;
import cn.mibcxb.android.map.Viewport;
import cn.mibcxb.android.map.projection.Mercator;
import cn.mibcxb.android.view.MapSurface.DrawingHandler;
import cn.mibcxb.android.view.overlay.background.Background;
import cn.mibcxb.android.view.overlay.background.Background.BackType;

public class OverlayManager {
    private DrawingHandler drawingHandler;

    private Background background;
    private final TileInfo tileInfo;
    private boolean tileInfoEnabled = false;

    private final Point tileTL = new Point();
    private final Point tileBR = new Point();
    private MapTile[] tiles;
    private Point[] positions;

    private final List<TileLayer> layerList = new ArrayList<TileLayer>();
    private final List<Overlay> overlayList = new ArrayList<Overlay>();

    public OverlayManager(Context context) {
        this.tileInfo = new TileInfo(context);
    }

    public void draw(Canvas canvas, Viewport projection) {
        updateTileSequence(projection);

        if (background != null) {
            if (background.getBackType() == BackType.BACK) {
                background.draw(canvas, projection);
            } else {
                background.draw(canvas, projection, tiles, positions);
            }
        }

        for (TileLayer layer : layerList) {
            layer.draw(canvas, projection, tiles, positions);
        }
        for (Overlay overlay : overlayList) {
            overlay.draw(canvas, projection);
        }

        if (tileInfoEnabled) {
            tileInfo.draw(canvas, projection, tiles, positions);
        }
    }

    private void updateTileSequence(Viewport projection) {
        int zoom = projection.getZoom();
        Rect viewport = projection.getBounds();
        Mercator mercator = projection.getMercator();

        int tileSize = mercator.getTileSize();
        tileTL.set(viewport.left / tileSize, viewport.top / tileSize);
        tileTL.offset(-1, -1);
        tileBR.set(viewport.right / tileSize, viewport.bottom / tileSize);
        tileBR.offset(1, 1);

        int m = tileBR.y - tileTL.y;
        int n = tileBR.x - tileTL.x;
        int count = m * n;
        if (null == tiles || tiles.length != count) {
            tiles = new MapTile[count];
            positions = new Point[count];
        }

        int k = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                final int y = tileTL.y + i;
                final int x = tileTL.x + j;
                final int tileY = mercator.clipTile(zoom, y);
                final int tileX = mercator.clipTile(zoom, x);
                tiles[k] = new MapTile(zoom, tileX, tileY);
                positions[k] = new Point(x * tileSize - viewport.left, y
                        * tileSize - viewport.top);
                k++;
            }
        }
    }

    public void detach() {
        for (TileLayer layer : layerList) {
            layer.detach();
        }
    }

    public void notifyChanged() {
        if (drawingHandler != null) {
            for (TileLayer layer : layerList) {
                layer.setHandler(drawingHandler);
            }
            drawingHandler.invalidate();
        }
    }

    public boolean addTileLayer(TileLayer layer) {
        if (layer != null && !layerList.contains(layer)) {
            return layerList.add(layer);
        }
        return false;
    }

    public boolean removeTileLayer(TileLayer layer) {
        return layerList.remove(layer);
    }

    public void clearTileLayerList() {
        layerList.clear();
    }

    public boolean addOverlay(Overlay overlay) {
        if (overlay != null && !overlayList.contains(overlay)) {
            return overlayList.add(overlay);
        }
        return false;
    }

    public boolean removeOverlay(Overlay overlay) {
        return overlayList.remove(overlay);
    }

    public void clearOverlayList() {
        overlayList.clear();
    }

    public DrawingHandler getDrawingHandler() {
        return drawingHandler;
    }

    public void setDrawingHandler(DrawingHandler drawingHandler) {
        this.drawingHandler = drawingHandler;
        notifyChanged();
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public boolean isTileInfoEnabled() {
        return tileInfoEnabled;
    }

    public void setTileInfoEnabled(boolean tileInfoEnabled) {
        this.tileInfoEnabled = tileInfoEnabled;
    }

    public boolean onClick(Viewport projection, MotionEvent e) {
        boolean flag = false;
        int size = overlayList.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                if (overlayList.get(i).onClick(projection, e)) {
                    flag = true;
                    break;
                }
            }
        }
        if (drawingHandler != null) {
            drawingHandler.invalidate();
        }
        return flag;
    }

    public boolean onDoubleClick(Viewport projection, MotionEvent e) {
        boolean flag = false;
        int size = overlayList.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                if (overlayList.get(i).onDoubleClick(projection, e)) {
                    flag = true;
                    break;
                }
            }
        }
        if (drawingHandler != null) {
            drawingHandler.invalidate();
        }
        return flag;
    }

    public void onLongPress(Viewport projection, MotionEvent e) {
        int size = overlayList.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                if (overlayList.get(i).onLongPress(projection, e)) {
                    break;
                }
            }
        }
        if (drawingHandler != null) {
            drawingHandler.invalidate();
        }
    }

    public boolean onScroll(Viewport projection, MotionEvent e1,
            MotionEvent e2, float distanceX, float distanceY) {
        boolean flag = false;
        int size = overlayList.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                if (overlayList.get(i).onScroll(projection, e1, e2, distanceX,
                        distanceY)) {
                    flag = true;
                    break;
                }
            }
        }
        if (drawingHandler != null) {
            drawingHandler.invalidate();
        }
        return flag;
    }

    public boolean onFling(Viewport projection, MotionEvent e1,
            MotionEvent e2, float velocityX, float velocityY) {
        boolean flag = false;
        int size = overlayList.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                if (overlayList.get(i).onFling(projection, e1, e2, velocityX,
                        velocityY)) {
                    flag = true;
                    break;
                }
            }
        }
        if (drawingHandler != null) {
            drawingHandler.invalidate();
        }
        return flag;
    }
}
