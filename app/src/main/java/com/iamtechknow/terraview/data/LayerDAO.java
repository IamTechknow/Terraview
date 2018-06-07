package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.iamtechknow.terraview.model.Layer;

import java.util.List;

@Dao
public interface LayerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Layer> layers);

    @Delete
    void delete(List<Layer> layers);

    @Query("SELECT * FROM layer")
    List<Layer> getLayers();
}