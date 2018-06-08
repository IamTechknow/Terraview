package com.iamtechknow.terraview.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.model.SearchQuery;
import com.iamtechknow.terraview.util.Utils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private WVDatabase db;
    private SharedPreferences prefs;
    private List<Layer> layers;
    private List<Measurement> measurements;
    private List<Category> categories;
    private HashMap<String, Layer> layerTable;

    public RemoteDataSource(Context c) {
        db = WVDatabase.getInstance(c);
        prefs = c.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Loads the data by downloading the XML and JSON with OkHttpClient and parsing through both.
     * Uses an observable to load the data in a background thread, then calls callbacks on the subscriber
     * @param callback Callbacks from the presenter to indicate completion or failure
     */
    @SuppressLint("CheckResult")
    @Override
    public void loadData(@NonNull LoadCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(XML_METADATA).build();
        Request jsonRequest = new Request.Builder().url(JSON_METADATA).build();

        Observable.just(true).map(aBoolean -> {
            try {
                db.clearAllTables();
                Response xmlResponse = client.newCall(request).execute();
                Response jsonResponse = client.newCall(jsonRequest).execute();

                WMTSReader reader = new WMTSReader();
                reader.run(xmlResponse.body().byteStream());
                WVJsonParser parser = new WVJsonParser(jsonResponse.body().byteStream());
                layers = reader.getResult();
                layerTable = Utils.getLayerTable(layers);
                parser.parse(layers, layerTable);
                measurements = parser.getMeasurements();
                categories = parser.getCategories();

                saveToDB(layers, measurements, categories, parser.getMeasureJoins(), parser.getCatJoins(), parser.getQueries());
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
            return true;
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aBoolean -> callback.onDataLoaded());
    }

    @Override
    public List<Layer> getLayers() {
        return layers;
    }

    @Override
    public Single<List<Layer>> getLayersForMeasurement(String m) {
        return db.getMeasureLayerJoinDao().getLayersForMeasurement(m);
    }

    @Override
    public Single<List<Measurement>> getMeasurementsForCategory(String c) {
        return db.getCatMeasureJoinDao().getMeasurementsforCategory(c);
    }

    @Override
    public List<Category> getCategories() {
        return categories;
    }

    @Override
    public HashMap<String, Layer> getLayerTable() {
        return layerTable;
    }

    /**
     * Helper function to save layer data to local database
     */
    private void saveToDB(List<Layer> data, List<Measurement> measures, List<Category> cats,
                          List<MeasureLayerJoin> measureJoins, List<CatMeasureJoin> catJoins, List<SearchQuery> queries) {
        db.getLayerDao().insert(data);
        db.getCategoryDao().insert(cats);
        db.getMeasurementDao().insert(measures);
        db.getMeasureLayerJoinDao().insert(measureJoins);
        db.getCatMeasureJoinDao().insert(catJoins);
        db.getSearchQueryDao().insert(queries);
        prefs.edit().putLong(PREFS_DB_KEY, System.currentTimeMillis()).apply();
        db.close();
    }
}
