package com.iamtechknow.worldview.colormaps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.model.ColorMap;
import com.iamtechknow.worldview.model.ColorMapEntry;

public class ColorMapViewImpl extends View implements ColorMapView {
    private float RECT_HEIGHT = dPToPixel();

    private Paint mPaint;

    private ColorMapPresenter presenter;

    private ColorMap colorMap;
    private float rectLength;

    public ColorMapViewImpl(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public ColorMapViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @Override
    public void setLayerId(String id) {
        presenter = new ColorMapPresenterImpl(this);
        presenter.parseColorMap(id);
    }

    //Called by the presenter when data is received
    @Override
    public void setColorMapData(ColorMap map) {
        colorMap = map;
        rectLength = getWidth() / colorMap.getList().size();
        invalidate(); //will call onDraw()
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int index = 0;
        if(colorMap != null)
            for(ColorMapEntry e : colorMap.getList()) {
                mPaint.setARGB(255, e.getR(), e.getG(), e.getB());
                //Log.d(getClass().getSimpleName(), String.format("drawRect(%f, %f, %f, %f)", index * rectLength, 0f, (index + 1) * rectLength, RECT_HEIGHT));
                canvas.drawRect(index * rectLength, 0f, (index + 1) * rectLength, RECT_HEIGHT, mPaint);
                index++;
            }
    }

    private float dPToPixel() {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.md_keylines), r.getDisplayMetrics());
    }
}
