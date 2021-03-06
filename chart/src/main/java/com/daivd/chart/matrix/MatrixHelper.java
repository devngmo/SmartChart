package com.daivd.chart.matrix;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewParent;

import com.daivd.chart.core.base.BaseChart;
import com.daivd.chart.listener.Observable;
import com.daivd.chart.listener.ChartGestureObserver;

import java.util.List;

/**
 * Created by huang on 2017/9/29.
 * 图表放大缩小协助类
 */

public class MatrixHelper extends Observable<ChartGestureObserver> implements ITouch, ScaleGestureDetector.OnScaleGestureListener {

    private static final int MAX_ZOOM = 5;
    public static final int MIN_ZOOM = 1;
    private float zoom = MIN_ZOOM; //缩放比例  不得小于1
    private int translateX; //以左上角为准，X轴位移的距离
    private int translateY;//以左上角为准，y轴位移的距离
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private boolean isCanZoom;
    private boolean isScale; //是否正在缩放
    private Rect originalRect; //原始大小
    private Rect zoomRect;
    private float mDownX, mDownY;
    private int pointMode; //屏幕的手指点个数


    public MatrixHelper(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, new OnChartGestureListener());
    }

    /**
     * 处理手势
     */
    @Override
    public boolean handlerTouchEvent(MotionEvent event) {
        if (isCanZoom) {
            mScaleGestureDetector.onTouchEvent(event);
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }



    /**
     * 判断是否需要接收触摸事件
     */
    @Override
    public void onDisallowInterceptEvent(BaseChart chart, MotionEvent event) {
        if (!isCanZoom) {
            return;
        }
        ViewParent parent = chart.getParent();
        if (zoomRect == null || originalRect == null) {
            parent.requestDisallowInterceptTouchEvent(false);
            return;
        }
        switch (event.getAction()&MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                pointMode = 1;
                //ACTION_DOWN的时候，赶紧把事件hold住
                mDownX = event.getX();
                mDownY = event.getY();
                if(originalRect.contains((int)mDownX,(int)mDownY)){ //判断是否落在图表内容区中
                    parent.requestDisallowInterceptTouchEvent(true);
                }else{
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                pointMode += 1;
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointMode > 1) {
                    parent.requestDisallowInterceptTouchEvent(true);
                    return;
                }
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                boolean isDisallowIntercept = true;
                if (Math.abs(disX) > Math.abs(disY)) {
                    if ((disX > 0 && toRectLeft()) || (disX < 0 && toRectRight())) { //向右滑动
                        isDisallowIntercept = false;
                    }
                } else {
                    if ((disY > 0 && toRectTop()) || (disY < 0 && toRectBottom())) {
                        isDisallowIntercept = false;
                    }
                }
                parent.requestDisallowInterceptTouchEvent(isDisallowIntercept);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pointMode -= 1;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                pointMode = 0;
                parent.requestDisallowInterceptTouchEvent(false);
        }

    }

    private boolean toRectLeft() {
        return translateX <= -(zoomRect.width() - originalRect.width()) / 2;
    }

    private boolean toRectRight() {
        return translateX >= (zoomRect.width() - originalRect.width()) / 2;
    }

    private boolean toRectBottom() {

        return translateY >= (zoomRect.height() - originalRect.height()) / 2;
    }

    private boolean toRectTop() {
        return translateY <= -(zoomRect.height() - originalRect.height()) / 2;
    }


    @Override
    public void notifyObservers(List<ChartGestureObserver> observers) {
        for (ChartGestureObserver observer : observers) {
            observer.onViewChanged(zoom, translateX, translateY);
        }
    }

    private float tempScale = MIN_ZOOM; //缩放比例  不得小于1

    class OnChartGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX += distanceX;
            translateY += distanceY;
            notifyObservers(observables);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        //双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isCanZoom) {
                float oldZoom = zoom;
                if (isScale) { //缩小
                    zoom = zoom / 1.5f;
                    if (zoom < 1) {
                        zoom = MIN_ZOOM;
                        isScale = false;
                    }
                } else { //放大
                    zoom = zoom * 1.5f;
                    if (zoom > MAX_ZOOM) {
                        zoom = MAX_ZOOM;
                        isScale = true;
                    }
                }
                float factor = zoom / oldZoom;
                resetTranslate(factor);
                notifyObservers(observables);
            }

            return true;
        }

        //单击
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (ChartGestureObserver observer : observables) {
                observer.onClick(e.getX(), e.getY());
            }
            return true;
        }
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        tempScale = this.zoom;

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float oldZoom = zoom;
        float scale = detector.getScaleFactor();
        this.zoom = (float) (tempScale * Math.pow(scale, 2));
        float factor = zoom / oldZoom;
        resetTranslate(factor);
        notifyObservers(observables);
        if (this.zoom > MAX_ZOOM) {
            this.zoom = MAX_ZOOM;
            return true;
        } else if (this.zoom < 1) {
            this.zoom = 1;
            return true;
        }
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


    /**
     * 重新计算偏移量
     * * @param factor
     */
    private void resetTranslate(float factor) {

        translateX = (int) (translateX * factor);
        translateY = (int) (translateY * factor);
    }

    /**
     * 获取图片内容的缩放大小
     *
     * @param providerRect 内容的大小
     * @return 缩放后内容的大小
     */
    public Rect getZoomProviderRect(Rect providerRect) {
        originalRect = providerRect;
        Rect scaleRect = new Rect();
        int oldw = providerRect.width();
        int oldh = providerRect.height();
        int newWidth = (int) (oldw * zoom);
        int newHeight = (int) (oldh * zoom);
        int maxTranslateX = Math.abs(newWidth - oldw) / 2;
        int maxTranslateY = Math.abs(newHeight - oldh) / 2;
        if (Math.abs(translateX) > maxTranslateX) {
            translateX = translateX > 0 ? maxTranslateX : -maxTranslateX;
        }
        if (Math.abs(translateY) > maxTranslateY) {
            translateY = translateY > 0 ? maxTranslateY : -maxTranslateY;
        }
        int offsetX = (newWidth - oldw) / 2;
        int offsetY = (newHeight - oldh) / 2;
        scaleRect.left = providerRect.left - offsetX - translateX;
        scaleRect.right = providerRect.right + offsetX - translateX;
        scaleRect.top = providerRect.top - offsetY - translateY;
        scaleRect.bottom = providerRect.bottom + offsetY - translateY;
        zoomRect = scaleRect;
        return scaleRect;
    }


    public boolean isCanZoom() {
        zoom = 1f;
        return isCanZoom;

    }

    public void setCanZoom(boolean canZoom) {
        isCanZoom = canZoom;
    }

    public float getZoom() {
        return zoom;
    }


}
