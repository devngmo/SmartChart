package com.daivd.chart.component;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.daivd.chart.component.base.IChartTitle;
import com.daivd.chart.component.base.PercentComponent;
import com.daivd.chart.data.style.FontStyle;

/**
 * Created by huang on 2017/9/29.
 * 绘制标题
 */

public class ChartTitle extends PercentComponent<String> implements IChartTitle {

    private static final float MAX_PERCENT =0.4f;
    private FontStyle fontStyle= new FontStyle();



    @Override
    public void setPercent(float percent) {
        if(percent > MAX_PERCENT){
            percent = MAX_PERCENT;
        }
        super.setPercent(percent);
    }

    @Override
    public void draw(Canvas canvas, String chartName, Paint paint) {
        fontStyle.fillPaint(paint);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        int textWidth = (int)paint.measureText(chartName);
        Rect rect = getRect();
        int startY = rect.centerY();
        int startX = rect.centerX();
        Path path = new Path();
        switch (direction) {
            case TOP:
            case BOTTOM:
                startY-= textHeight/2;
                startX -=textWidth/2;
                canvas.drawText(chartName, startX, startY, paint);
                break;
            case LEFT:
            case RIGHT:
                path.moveTo(startX,rect.top);
                path.lineTo(startX,rect.bottom);
                canvas.drawTextOnPath(chartName,path,(rect.height()-textWidth)/2,0,paint);
                break;
        }
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }
}
