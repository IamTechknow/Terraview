package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.model.ColorMap;


public interface ColorMapContract {
    /**
     * View for the color map. When created, allows the fragment to send the identifier to the view,
     * which goes to the presenter to parse. The presenter calls setColorMapData() to have the view draw the color map.
     */
    interface View {
        void setColorMapData(ColorMap map);

        void setLayerId(String id);
    }

    /**
     * Presenter for the color map view that parses the XML and give the view the data to draw with
     */
    interface Presenter {
        void detachView();

        void parseColorMap(String id);
    }
}
