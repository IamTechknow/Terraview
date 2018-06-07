package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.model.SearchQuery;

@Database(entities = {Layer.class, Measurement.class, Category.class, SearchQuery.class, CatMeasureJoin.class, MeasureLayerJoin.class}, version = 1)
public abstract class WVDatabase extends RoomDatabase {
    private static WVDatabase INSTANCE;

    public static WVDatabase getInstance(Context c) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(c.getApplicationContext(),
                    WVDatabase.class, "layers.sqlite")
                    .build();
        }
        return INSTANCE;
    }

    public abstract LayerDAO getLayerDao();
    public abstract MeasurementDAO getMeasurementDao();
    public abstract CategoryDAO getCategoryDao();
    public abstract SearchDAO getSearchQueryDao();
    public abstract CatMeasureJoinDAO getCatMeasureJoinDao();
    public abstract MeasureLayerJoinDAO getMeasureLayerJoinDao();
}
