package com.daivd.chart.data;

import android.graphics.Rect;

import com.daivd.chart.axis.AxisDirection;
import com.daivd.chart.matrix.MatrixHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 2017/9/26.
 */

public class ScaleData {

    public boolean isLeftHasValue = false;
    public boolean isRightHasValue = false;
    //竖轴数据
    public double maxLeftValue; //最大值
    public double minLeftValue; //最小值

    //竖轴数据
    public double maxRightValue; //最大值
    public double minRightValue; //最小值

    public int totalScale = 5; //刻度数量

    public Rect scaleRect = new Rect();
    public Rect legendRect = new Rect();
    public Rect titleRect = new Rect();
    //横轴数据
    public int  rowSize; //列数据
    public float zoom = MatrixHelper.MIN_ZOOM; //放大比例


    public List<Double> getScaleList(AxisDirection direction){

        List<Double> scaleList = new ArrayList<>();
        int total = (int) (totalScale *zoom);
        double scale = getTotalScaleLength(direction) /total;
        double minValue = getMinScaleValue(direction);
        for(int i = 0;i < total;i++){
            scaleList.add(minValue +scale*i);
        }
        return scaleList;
    }

    /**
     * 获取最大刻度
     * @param direction
     * @return
     */
    public double getMaxScaleValue(AxisDirection direction){
        if(direction == AxisDirection.LEFT){
            return  maxLeftValue;
        }
        return  maxRightValue;
    }

    /**
     * 获取最小刻度
     * @param direction
     * @return
     */
    public double getMinScaleValue(AxisDirection direction){
        if(direction == AxisDirection.LEFT){
            return  minLeftValue;
        }
        return  minRightValue;
    }

    /**
     *
     * @param direction
     * @return
     */
    public double getTotalScaleLength(AxisDirection direction){
        if(direction == AxisDirection.LEFT){
            return maxLeftValue - minLeftValue;
        }
        return  maxRightValue - minRightValue;
    }

    public Rect getOffsetRect(Rect rect, Rect offsetRect){
        rect.left = rect.left + offsetRect.left;
        rect.right = rect.right - offsetRect.right;
        rect.top = rect.top + offsetRect.top;
        rect.bottom = rect.bottom - offsetRect.bottom;
        return rect;

    }


}