package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

@Dao
public interface MeasurementDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Measurement... measurements);

    @Delete
    void delete(Measurement... measurements);

    @Query("SELECT * FROM measurement")
    List<Measurement> getMeasurements();

    @Query("SELECT * FROM measurement WHERE value = :category")
    List<Measurement> getMeasurementsForCategory(String category);
}
