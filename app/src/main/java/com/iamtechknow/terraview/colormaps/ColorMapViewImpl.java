package com.iamtechknow.terraview.colormaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * View implementation for the color map list item. Needs a reference to the presenter
 * to prevent it from being GCed, and to allow it to be called to draw the color map
 */
public class ColorMapViewImpl extends View implements ColorMapContract.View {
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";

    private float RECT_HEIGHT = Utils.dPToPixel(getResources(), R.dimen.md_keylines);

    //Controls the color for drawing the color map
    private Paint mPaint;

    //Presenter half of the MVP contract
    private ColorMapContract.Presenter presenter;

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
        //Dependency injection
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        presenter = new ColorMapPresenterImpl(this, retrofit.create(ColorMapAPI.class));
        presenter.parseColorMap(id);
    }

    //Called by the presenter when data is received. Calculate the length of each rectangle
    //Then invalidate the view to have onDraw() be called. Seekbar is set when colormap exists
    @Override
    public void setColorMapData(ColorMap map) {
        colorMap = map.getList();
        rectLength = (float) getWidth() / (float) colorMap.size();

        View parent = (View) getParent();
        parent.findViewById(R.id.empty_view).setVisibility(View.GONE);
        val = ((View) getParent()).findViewById(R.id.color_map_val);
        val.setVisibility(View.VISIBLE);

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
