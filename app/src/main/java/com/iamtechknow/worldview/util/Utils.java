package com.iamtechknow.worldview.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;

import com.iamtechknow.worldview.R;

public class Utils {
    public static boolean isOnline(Context c) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static void showAbout(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null)
            ft.remove(prev);

        ft.addToBackStack(null);

        new aboutDialog().show(ft, "dialog_about");
    }

    public static class aboutDialog extends DialogFragment {
        public static final String MIME_TYPE = "text/html";

        public aboutDialog() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadData(getString(R.string.about_html), MIME_TYPE, null);

            return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.about)
                .setView(webView)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
        }
    }
}
