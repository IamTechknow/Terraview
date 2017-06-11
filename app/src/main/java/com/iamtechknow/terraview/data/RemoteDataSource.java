package com.iamtechknow.terraview.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of a remote data source by communicating with the NASA GIBS to obtain and parse data.
 */
public class RemoteDataSource implements DataSource {
    private static final String XML_METADATA = "https://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml",
                                JSON_METADATA = "https://worldview.earthdata.nasa.gov/config/wv.json",
                                PREFS_FILE = "settings", PREFS_DB_KEY = "last_update";

    //Data
    private LayerDatabase db;
    private SharedPreferences prefs;
    private ArrayList<Layer> layers;
    private TreeMap<String, ArrayList<String>> categories, measurements;
    private Hashtable<String, Layer> layerTable;

    public RemoteDataSource(Context c) {
        db = LayerDatabase.getInstance(c);
        db.reset();
        prefs = c.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Loads the data by downloading the XML and JSON with OkHttpClient and parsing through both.
     * Uses an observable to load the data in a background thread, then calls callbacks on the subscriber
     * @param callback Callbacks from the presenter to indicate completion or failure
     */
    @Override
    public void loadData(@NonNull LoadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(XML_METADATA).build();
        Request jsonRequest = new Request.Builder().url(JSON_METADATA).build();

        Observable.just(true).map(aBoolean -> {
            try {
                Response xmlResponse = client.newCall(request).execute();
                Response jsonResponse = client.newCall(jsonRequest).execute();

                WMTSReader reader = new WMTSReader();
                reader.run(xmlResponse.body().byteStream());
                WVJsonParser parser = new WVJsonParser(jsonResponse.body().byteStream());
                layers = reader.getResult();
                parser.parse(layers);
                measurements = parser.getMeasurementMap();
                categories = parser.getCategoryMap();
                layerTable = Utils.getLayerTable(layers);

                saveToDB(layers, measurements, categories);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
            return true;
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Boolean>() {
              @Override
              public void onSubscribe(Disposable disposable) {}

              @Override
              public void onComplete() {
                  callback.onDataLoaded();
              }

              @Override
              public void onError(Throwable e) {
                  Log.e(getClass().getSimpleName(), e.getMessage());
                  callback.onDataNotAvailable();
              }

              @Override
              public void onNext(Boolean b) {}
          });
    }

    @Override
    public ArrayList<Layer> getLayers() {
        return layers;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getMeasurements() {
        return measurements;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getCategories() {
        return categories;
    }

    @Override
    public Hashtable<String, Layer> getLayerTable() {
        return layerTable;
    }

    /**
     * Helper function to save layer data to local database
     * @param data The parsed data
     */
    private void saveToDB(ArrayList<Layer> data, TreeMap<String, ArrayList<String>> measures, TreeMap<String, ArrayList<String>> cats) {
        db.insertLayers(data);
        db.insertCategories(cats);
        db.insertMeasurements(measures);
        prefs.edit().putLong(PREFS_DB_KEY, System.currentTimeMillis()).apply();
        db.close();
    }
}
