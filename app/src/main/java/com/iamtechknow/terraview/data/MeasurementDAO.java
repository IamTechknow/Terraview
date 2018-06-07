package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

//DAO to allow inserting and deleting measurements. Joins are defined in the relevant DAO
@Dao
public interface MeasurementDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Measurement> measurements);

    @Delete
    void delete(List<Measurement> measurements);

    @Query("SELECT * FROM measurement")
    List<Measurement> getMeasurements();
}
