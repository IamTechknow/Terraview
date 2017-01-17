package com.iamtechknow.terraview.colormaps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;

/**
 * View implementation for the color map list item. Needs a reference to the presenter
 * to prevent it from being GCed, and to allow it to be called to draw the color map
 */
public class ColorMapViewImpl extends View implements ColorMapView {
    private float RECT_HEIGHT = dPToPixel(R.dimen.md_keylines);

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(presenter != null)
            presenter.detachView();
    }

    @Override
    public void setLayerId(String id) {
        presenter = new ColorMapPresenterImpl();
        presenter.attachView(this);
        presenter.parseColorMap(id);
    }

    //Called by the presenter when data is received. Calculate the length of each rectangle
    //Then invalidate the view to have onDraw() be called
    @Override
    public void setColorMapData(ColorMap map) {
        colorMap = map;
        rectLength = (float) getWidth() / (float) colorMap.getList().size();
        invalidate(); //will call onDraw()
    }

    /**
     * Iterate through the color map to set the paint object's RGB color,
     * then calculate the width of the rectangle to draw. Do this for all color map entries.
     * Finally get the text views for the start and end labels and display them
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int index = 0;
        if(colorMap != null) {
            for (ColorMapEntry e : colorMap.getList()) {
                mPaint.setARGB(255, e.getR(), e.getG(), e.getB());
                canvas.drawRect(index * rectLength, 0f, (index + 1) * rectLength, RECT_HEIGHT, mPaint);
                index++;
            }
            View parent = (View) getParent();
            TextView start = (TextView) parent.findViewById(R.id.color_map_start),
                    end = (TextView) parent.findViewById(R.id.color_map_end);
            start.setText(colorMap.getList().get(0).getLabel());
            end.setText(colorMap.getList().get(index - 1).getLabel());
        }
    }

    /**
     * Respond to events in the canvas itself. Calculate the color that is being tapped,
     * then show a popup that displays the value of the color being tapped.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        int index = Math.max(0, (int) (x / getWidth() * (colorMap.getList().size() - 1))); //percentage from right edge x size
        Log.d(getClass().getSimpleName(), "X: " + x + " Value: " + colorMap.getList().get(index).getLabel() + " Index: " + index + " / " + (colorMap.getList().size() - 1));
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: //show tooltip

                break;
            case MotionEvent.ACTION_MOVE: //move tooltip

                break;
            case MotionEvent.ACTION_UP: //hide tooltip

                break;
            default:
        }
        return true;
    }

    /**
     * Do some preprocessing by converting the DP dimension to pixels to get the rectangle height
     * @return pixel size of the DP
     */
    private float dPToPixel(int dimen) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(dimen), r.getDisplayMetrics());
    }
}
