package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface MeasureLayerJoinDAO {
    @Insert
    void insert(List<MeasureLayerJoin> join);

    @Query("SELECT measurement.* FROM measurement INNER JOIN measure_layer_join ON measurement.name=measure_layer_join.measurement WHERE measure_layer_join.layer=:layer")
    List<Measurement> getMeasurementsForLayer(String layer);

    @Query("SELECT layer.* FROM layer INNER JOIN measure_layer_join ON layer.identifier=measure_layer_join.layer WHERE measure_layer_join.measurement=:measurement")
    Single<List<Layer>> getLayersForMeasurement(String measurement);
}
