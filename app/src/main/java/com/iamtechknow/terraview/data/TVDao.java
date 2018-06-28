package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.model.SearchQuery;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface TVDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLayers(List<Layer> layers);

    @Query("SELECT * FROM layer")
    Single<List<Layer>> getLayers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);

    @Query("SELECT * FROM category")
    Single<List<Category>> getCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMeasurements(List<Measurement> measurements);

    @Query("SELECT * FROM measurement")
    Single<List<Measurement>> getMeasurements();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQueries(List<SearchQuery> queries);

    //The like query is query%, which must be substituted by the arg variable for this to work
    @Query("SELECT _id, suggest_text_1, suggest_intent_extra_data FROM search WHERE suggest_text_1 LIKE :arg")
    Cursor searchQuery(String arg);
}
