package com.iamtechknow.worldview;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.iamtechknow.worldview.model.WMTSReader;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends IntentService {
    public static final String URL_EXTRA = "url", PENDING_RESULT_EXTRA = "result", RESULT_LIST = "list";

    private static final String TAG = DownloadService.class.getSimpleName();

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OkHttpClient client = new OkHttpClient();

        try {
            String url = intent.getStringExtra(URL_EXTRA);
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            WMTSReader reader = new WMTSReader();
            reader.run(response.body().byteStream());

            //run() calls WMTSHandler.parse() which is noy async, so we may get result when we're done


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
