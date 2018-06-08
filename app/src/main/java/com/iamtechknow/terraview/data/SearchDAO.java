package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.iamtechknow.terraview.model.SearchQuery;

import java.util.List;

import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;

@Dao
public interface SearchDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<SearchQuery> queries);

    @Delete
    void delete(List<SearchQuery> queries);

    //The like query is query%, which must be substituted by the arg variable for this to work
    @Query("SELECT _id, suggest_text_1, suggest_intent_extra_data FROM search WHERE suggest_text_1 LIKE :arg")
    Cursor searchQuery(String arg);
}
