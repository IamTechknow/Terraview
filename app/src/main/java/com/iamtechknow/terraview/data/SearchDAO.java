package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.iamtechknow.terraview.model.SearchQuery;

import java.util.List;

import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;

public interface SearchDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<SearchQuery> queries);

    @Delete
    void delete(List<SearchQuery> queries);

    @Query("SELECT _id, " + SUGGEST_COLUMN_TEXT_1 + ", " + SUGGEST_COLUMN_INTENT_EXTRA_DATA + " FROM search WHERE " + SUGGEST_COLUMN_TEXT_1 + " like ':arg%%'")
    Cursor searchQuery(String arg);
}
