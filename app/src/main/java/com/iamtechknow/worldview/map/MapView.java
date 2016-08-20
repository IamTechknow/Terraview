package com.iamtechknow.worldview.map;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

/**
 * Contract for the view. Not all methods are called by the presenter but can be called as a result of user input, thus must be implemented
 */
public interface MapView {
    void setDateDialog(long maxDate);

    void setLayerList(ArrayList<Layer> stack);
}
