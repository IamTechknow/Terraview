package com.iamtechknow.terraview.map;

import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;

/**
 * Contract for the view. Not all methods are called by the presenter but can be called as a result of user input, thus must be implemented
 */
public interface MapView {
    void setDateDialog(long today);

    void updateDateDialog(long currDate);

    void setLayerList(ArrayList<Layer> stack);

    void openEmail();

    void showColorMaps();

    void showPicker();

    void showAbout();
}
