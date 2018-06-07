package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

//Interface to allow obtaining measurements for a given category (and vice versa, but not used here)
@Dao
public interface CatMeasureJoinDAO {
    @Insert
    void insert(List<CatMeasureJoin> join);

    @Query("SELECT * FROM category INNER JOIN cat_measure_join ON category.name=cat_measure_join.category WHERE cat_measure_join.measurement=:measurement")
    List<Category> getCategoriesforMeasurement(String measurement);

    @Query("SELECT * FROM measurement INNER JOIN cat_measure_join ON measurement.name=cat_measure_join.measurement WHERE cat_measure_join.category=:category")
    List<Measurement> getMeasurementsforCategory(String category);
}
