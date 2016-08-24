package com.iamtechknow.worldview.colormaps;

import com.iamtechknow.worldview.model.ColorMap;

/**
 * View for the color map. When created, allows the fragment to send the identifier to the view,
 * which goes to the presenter to parse. The presenter calls setColorMapData() to have the view draw the color map.
 */
public interface ColorMapView {
    void setColorMapData(ColorMap map);

    void setLayerId(String id);
}
