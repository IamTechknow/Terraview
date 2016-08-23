package com.iamtechknow.worldview.colormaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.iamtechknow.worldview.model.ColorMap;

public class ColorMapViewImpl extends View implements ColorMapView{
    private Paint mPaint;

    private String layer_id; //TODO: set presenter for view on fragment setup, view tells presenter to get colorMap, then when callback done call setColorMap here
    private ColorMap colorMap;
    private double rectLength;

    public ColorMapViewImpl(Context context) {
        super(context);

        mPaint = new Paint();
    }

    public ColorMapViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setColorMap(ColorMap map) {
        colorMap = map;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
