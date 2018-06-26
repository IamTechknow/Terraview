package com.iamtechknow.terraview.colormaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

/**
 * View implementation for the color map list item.
 */
public class ColorMapViewImpl extends View {
    private float RECT_HEIGHT = Utils.dPToPixel(getResources(), R.dimen.md_keylines);

    //Controls the color for drawing the color map
    private Paint mPaint;

    //List of all color map entries
    private ArrayList<ColorMapEntry> colorMap;

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

    //Clear empty view, invalidate the view to have onDraw() be called
    public void setColorMapData(ColorMap map) {
        colorMap = map.getList();

        View parent = (View) getParent();
        parent.findViewById(R.id.empty_view).setVisibility(View.GONE);
        val = parent.findViewById(R.id.color_map_val);
        val.setVisibility(View.VISIBLE);

        invalidate(); //will call onDraw()
    }

    /**
     * Iterate through the color map to set the paint object's RGB color,
     * then calculate the width (which should be non-zero by now) of the rectangle to draw.
     * Finally get the text views for the start and end labels and display them
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int index = 0;
        if(colorMap != null) {
            //Length of each rectangle being drawn depending on the color map size
            float rectLength = (float) getWidth() / (float) colorMap.size();

            for (ColorMapEntry e : colorMap) {
                mPaint.setARGB(255, e.getR(), e.getG(), e.getB());
                canvas.drawRect(index * rectLength, 0f, (index + 1) * rectLength, RECT_HEIGHT, mPaint);
                index++;
            }
            View parent = (View) getParent();
            TextView start = parent.findViewById(R.id.color_map_start),
                    end = parent.findViewById(R.id.color_map_end);
            start.setText(colorMap.get(0).getLabel());
            end.setText(colorMap.get(index - 1).getLabel());
            val.setText(colorMap.get(index / 2).getLabel());
        }
    }

    /**
     * Colorbar is tapped or swiped, determine the color map entry tapped and show its value
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int index = (int) (event.getX() / getWidth() * colorMap.size());
                if(index >= 0 && index < colorMap.size())
                    val.setText(colorMap.get(index).getLabel());
        }
        return true;
    }
}
