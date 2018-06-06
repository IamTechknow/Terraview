package com.iamtechknow.terraview.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;

@Entity(tableName = "search")
public class SearchQuery {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private final int id;

    @ColumnInfo(name = SUGGEST_COLUMN_TEXT_1)
    private final String suggestion;

    @ColumnInfo(name = SUGGEST_COLUMN_INTENT_EXTRA_DATA)
    private final String layerId;

    public SearchQuery(int id, String suggestion, String layerId) {
        this.id = id;
        this.suggestion = suggestion;
        this.layerId = layerId;
    }

    public int getId() {
        return id;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getLayerId() {
        return layerId;
    }
}
