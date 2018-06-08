package com.iamtechknow.terraview.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * A mechanism to provide autocomplete functionality for built-in searching to select layers.
 */
public class LayerProvider extends ContentProvider {
    public static final String AUTHORITY = "com.iamtechknow.terraview.LayerProvider";
    private static final int SEARCH_SUGGEST = 0;

    private SearchDAO dao;
    private UriMatcher matcher;

    @Override
    public boolean onCreate() {
        dao = WVDatabase.getInstance(getContext()).getSearchQueryDao();
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch(matcher.match(uri)) {
            case SEARCH_SUGGEST:
                return dao.searchQuery(selectionArgs[0] + "%"); //Add the % here so LIKE clause can work
            default:
                return null;
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    //Required but unused operations for searching
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
