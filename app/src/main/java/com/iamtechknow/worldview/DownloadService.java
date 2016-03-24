package com.iamtechknow.worldview;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.iamtechknow.worldview.model.LayerDatabase;
import com.iamtechknow.worldview.model.WMTSReader;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An IntentService that automatically handles tasks in the background, including downloading and parsing
 */
public class DownloadService extends IntentService {
    public static final String URL_EXTRA = "url", PENDING_RESULT_EXTRA = "result", RESULT_LIST = "list",
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
            String url = intent.getStringExtra(URL_EXTRA);
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            WMTSReader reader = new WMTSReader();
            reader.run(response.body().byteStream());

            //run() calls WMTSHandler.parse() which is also synchronous so we may get result when we're done
            LayerDatabase db = new LayerDatabase(this);
            db.insertLayers(reader.getResult());
            getSharedPreferences(PREFS_FILE, MODE_PRIVATE).edit().putBoolean(PREFS_DB_KEY, true).apply();
            db.close();

            PendingIntent p = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
            Bundle b = new Bundle();
            b.putParcelableArrayList(RESULT_LIST, reader.getResult());
            Intent i = new Intent().putExtras(b);

            p.send(this, android.app.Activity.RESULT_OK, i);
        } catch (IOException | ParserConfigurationException | SAXException | PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
