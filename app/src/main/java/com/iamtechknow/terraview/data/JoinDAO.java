package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

import io.reactivex.Single;

//Interface to allow obtaining measurements for a given category (and vice versa, but not used here)
@Dao
public interface JoinDAO {
    @Insert
    void insertCatMeasureJoin(List<CatMeasureJoin> join);

    @Query("SELECT measurement.* FROM measurement INNER JOIN cat_measure_join ON measurement.name=cat_measure_join.measurement WHERE cat_measure_join.category=:category")
    Single<List<Measurement>> getMeasurementsForCategory(String category);

    @Insert
    void insertMeasureLayerJoin(List<MeasureLayerJoin> join);

    @Query("SELECT layer.* FROM layer INNER JOIN measure_layer_join ON layer.identifier=measure_layer_join.layer WHERE measure_layer_join.measurement=:measurement")
    Single<List<Layer>> getLayersForMeasurement(String measurement);
}
