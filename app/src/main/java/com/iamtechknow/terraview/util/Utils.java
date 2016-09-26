package com.iamtechknow.terraview.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.iamtechknow.terraview.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final String HTML_EXTRA = "html", ABOUT_TAG = "dialog_about", ISO_FMT = "yyyy-MM-dd", DIALOG_FMT = "EEE, MMM dd, yyyy";
    private static final String TAG = "Utils";

    private static final SimpleDateFormat ISO = new SimpleDateFormat(ISO_FMT, Locale.US);
    private static final SimpleDateFormat DIALOG = new SimpleDateFormat(DIALOG_FMT, Locale.US);

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
     * Helper method to parse a Date object into a ISO 8601 date String for REST transactions
     * @return the date in a String format
     */
    public static String parseDate(Date d) {
        return ISO.format(d);
    }

    /**
     * Helper method to parse a Date object into the String format EEE, MMM dd, yyyy for the dialog
     * @return the date in a String format
     */
    public static String parseDateForDialog(Date d) {
        return DIALOG.format(d);
    }

    /**
     * Given a formatted standard ISO 8601 string, get the corresponding date object.
     * @param date The ISO 8601 string
     * @return Returns the date object assuming correct input
     */
    public static Date parseISODate(String date) {
        Date result = null;

        try {
            result = ISO.parse(date);
        } catch (ParseException e) {
            Log.w(TAG, e);
        }

        return result;
    }

    public static Date parseDialogDate(String date) {
        Date result = null;

        try {
            result = DIALOG.parse(date);
        } catch (ParseException e) {
            Log.w(TAG, e);
        }

        return result;
    }

    /**
     * Set the calendar time to midnight
     * @param c The calendar object to use
     */
    public static void getCalendarMidnightTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
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
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> dialog.dismiss()).create();
        }
    }
}
