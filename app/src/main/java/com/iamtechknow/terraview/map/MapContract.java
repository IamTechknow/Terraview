package com.iamtechknow.terraview.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;
import java.util.Date;

public interface MapContract {
    /**
     * Contract for the view. Not all methods are called by the presenter but can be called as a result of user input, thus must be implemented
     */
    interface View {
        void setDateDialog(long today);

        void updateDateDialog(long currDate);

        void setLayerList(ArrayList<Layer> stack);

        void showColorMaps();

        void showPicker();

        void showEvents();

        void showAbout();

        void showHelp();

        void showAnimDialog();

        void showEvent(Event e);

        void clearEvent();

        void updateEventDateText(String date);

        void warnUserAboutActiveLayers();

        void warnNoLayersToAnim();

        void showChangedEventDate(String date);

        void showColorMap(ColorMap show);
    }

    /**
     * Presenter portion of the Map contract, exposes all methods needed by the map view.
     */
    interface Presenter extends OnMapReadyCallback, MapInteractor.ToggleListener {
        void detachView();

        void onRestoreInstanceState(Bundle savedInstanceState);

        void onDateChanged(Date date);

        Date getCurrDate();

        Event getCurrEvent();

        void getRemoteData(Context c);

        void getLocalData(LoaderManager manager, Context c);

        void setLayersAndUpdateMap(ArrayList<Layer> stack, ArrayList<Layer> delete);

        void onSwapNeeded(int i, int i_new);

        void onToggleLayer(Layer l, int visibility);

        void onLayerSwiped(int position, Layer l);

        ArrayList<Layer> getCurrLayerStack();

        void presentColorMaps();

        void presentAbout();

        void presentHelp();

        void presentAnimDialog();

        void chooseLayers();

        void presentEvents();

        void presentEvent(Event e);

        void onClearEvent();

        boolean isVIIRSActive();

        void fixVIIRS();

        void onEventProgressChanged(int progress);

        void onEventProgressSelected(int progress);
    }
}
