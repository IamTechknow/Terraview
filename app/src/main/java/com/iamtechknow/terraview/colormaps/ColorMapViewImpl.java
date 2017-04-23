package com.iamtechknow.terraview.colormaps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;

import java.util.ArrayList;

/**
 * View implementation for the color map list item. Needs a reference to the presenter
 * to prevent it from being GCed, and to allow it to be called to draw the color map
 */
public class ColorMapViewImpl extends View implements ColorMapView, SeekBar.OnSeekBarChangeListener {
    private float RECT_HEIGHT = dPToPixel(R.dimen.md_keylines);

    //Controls the color for drawing the color map
    private Paint mPaint;

    //Presenter half of the MVP contract
    private ColorMapPresenter presenter;

    //List of all color map entries
    private ArrayList<ColorMapEntry> colorMap;

    //Length of each rectangle being drawn depending on the color map size
    private float rectLength;

    //Text view of the current value being shown in the color map by the slider
    private TextView val;

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
        val = (TextView) getRootView().findViewById(R.id.color_map_val);
        val.setVisibility(View.VISIBLE);

        presenter = new ColorMapPresenterImpl();
        presenter.attachView(this);
        presenter.parseColorMap(id);
    }

    //Called by the presenter when data is received. Calculate the length of each rectangle
    //Then invalidate the view to have onDraw() be called. Seekbar is set when colormap exists
    @Override
    public void setColorMapData(ColorMap map) {
        colorMap = map.getList();
        rectLength = (float) getWidth() / (float) colorMap.size();

        View parent = (View) getParent();
        SeekBar bar = (SeekBar) parent.findViewById(R.id.color_map_picker);
        bar.setVisibility(View.VISIBLE);
        bar.setOnSeekBarChangeListener(this);

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
            for (ColorMapEntry e : colorMap) {
                mPaint.setARGB(255, e.getR(), e.getG(), e.getB());
                canvas.drawRect(index * rectLength, 0f, (index + 1) * rectLength, RECT_HEIGHT, mPaint);
                index++;
            }
            View parent = (View) getParent();
            TextView start = (TextView) parent.findViewById(R.id.color_map_start),
                    end = (TextView) parent.findViewById(R.id.color_map_end);
            start.setText(colorMap.get(0).getLabel());
            end.setText(colorMap.get(index - 1).getLabel());
            val.setText(colorMap.get(index / 2).getLabel());
        }
    }

    /**
     * Slider value has been changed, update the current value text by calculating
     * the index being mapped by the new progress percentage.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float index = ((float) progress) / 100 * (colorMap.size() - 1); //percentage x size
        val.setText(colorMap.get((int) index).getLabel());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /**
     * Do some preprocessing by converting the DP dimension to pixels to get the rectangle height
     * @return pixel size of the DP
     */
    private float dPToPixel(int dimen) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(dimen), r.getDisplayMetrics());
    }
}
