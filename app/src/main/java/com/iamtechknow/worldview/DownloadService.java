package com.iamtechknow.worldview;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerDatabase;
import com.iamtechknow.worldview.model.WMTSReader;
import com.iamtechknow.worldview.model.WVJsonParser;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An IntentService that automatically handles tasks in the background, including downloading and parsing
 */
public class DownloadService extends IntentService {
    public static final String URL_JSON = "json", URL_XML = "xml", PENDING_RESULT_EXTRA = "result", RESULT_LIST = "list",
            PREFS_FILE = "settings", PREFS_DB_KEY = "have_db";

    private static final String TAG = DownloadService.class.getSimpleName();

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OkHttpClient client = new OkHttpClient();

        try {
            //Get the URL and build the HTTP request, then get the response. These calls are synchronous
            String xml = intent.getStringExtra(URL_XML), json = intent.getStringExtra(URL_JSON);
            Request request = new Request.Builder().url(xml).build();
            Request jsonRequest = new Request.Builder().url(json).build();
            Response xmlResponse = client.newCall(request).execute();
            Response jsonResponse = client.newCall(jsonRequest).execute();

            //Get GIBS data from XML, then metadata from JSON, both of which have exclusive data
            //run() calls WMTSHandler.parse() which is also synchronous so we may get result when we're done
            WMTSReader reader = new WMTSReader();
            reader.run(xmlResponse.body().byteStream());
            WVJsonParser parser = new WVJsonParser(jsonResponse.body().byteStream());
            ArrayList<Layer> list = reader.getResult();
            parser.parse(list);

            saveToDB(list);

            //Send data back to WorldActivity
            PendingIntent p = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
            Bundle b = new Bundle();
            b.putParcelableArrayList(RESULT_LIST, list);
            Intent i = new Intent().putExtras(b);
            p.send(this, android.app.Activity.RESULT_OK, i);
        } catch (IOException | ParserConfigurationException | SAXException | PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function to save layer data to local database
     * @param data The parsed data
     */
    private void saveToDB(ArrayList<Layer> data) {
        LayerDatabase db = new LayerDatabase(this);
        db.insertLayers(data);
        getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit().putBoolean(PREFS_DB_KEY, true).apply();
        db.close();
    }
}
