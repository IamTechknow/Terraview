package com.iamtechknow.worldview;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import com.iamtechknow.worldview.model.WMTSReader;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends IntentService {
    public static final String URL_EXTRA = "url";
    public static final String PENDING_RESULT_EXTRA = "result";

    private static final String TAG = DownloadService.class.getSimpleName();

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingIntent p = intent.getParcelableExtra(PENDING_RESULT_EXTRA);

        OkHttpClient client = new OkHttpClient();

        try {
            String url = intent.getStringExtra(URL_EXTRA);
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            WMTSReader reader = new WMTSReader();
            reader.run(response.body().byteStream());

            //p.send(this, , );
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }
}
