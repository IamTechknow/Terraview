package com.iamtechknow.terraview.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;

@Entity(tableName = "search")
public class SearchQuery {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = SUGGEST_COLUMN_TEXT_1)
    private String title;

    @ColumnInfo(name = SUGGEST_COLUMN_INTENT_EXTRA_DATA)
    private String layerId;

    @Ignore
    public SearchQuery(String title, String layerId) {
        this.title = title;
        this.layerId = layerId;
    }

    public SearchQuery(int id, String title, String layerId) {
        this.id = id;
        this.title = title;
        this.layerId = layerId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLayerId() {
        return layerId;
    }
}
