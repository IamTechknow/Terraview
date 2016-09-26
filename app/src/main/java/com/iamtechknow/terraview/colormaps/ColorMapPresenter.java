package com.iamtechknow.terraview.colormaps;

/**
 * Presenter for the color map view that parses the XML and give the view the data to draw with
 */
public interface ColorMapPresenter {
    void attachView(ColorMapView v);

    void detachView();

    void parseColorMap(String id);
}
