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
    private static final String HTML_EXTRA = "html", ABOUT_TAG = "dialog_about";

    /**
     * Helper method to determine whether or not there is internet access
     * @param c Context required to get the connectivity manager
     * @return Whether or not there is internet access
     */
    public static boolean isOnline(Context c) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Create and show the given HTML, essentially a formatted web page
     * @param activity The activity in which to display the fragment in
     */
    public static void showAbout(Activity activity) {
        showWebPage(activity, activity.getString(R.string.about_html));
    }

    public static void showWebPage(Activity activity, String html) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(ABOUT_TAG);
        Bundle extra1 = new Bundle();
        extra1.putString(HTML_EXTRA, html);
        if (prev != null)
            ft.remove(prev);

        ft.addToBackStack(null);

        AboutDialog about = new AboutDialog();
        about.setArguments(extra1);
        about.show(ft, ABOUT_TAG);
    }

    public static class AboutDialog extends DialogFragment {
        public static final String MIME_TYPE = "text/html";

        public AboutDialog() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadData(getArguments().getString(HTML_EXTRA), MIME_TYPE, null);

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
