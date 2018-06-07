package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

//Define the relationships between objects in both tables
@Entity(tableName = "cat_measure_join",
    primaryKeys = { "cat", "measure" },
    foreignKeys = {
        @ForeignKey(entity = Category.class,
            parentColumns = "name", //name refers to the name column in both models
            childColumns = "category"),
        @ForeignKey(entity = Measurement.class,
            parentColumns = "name",
            childColumns = "measurement")
})
public class CatMeasureJoin {
    public final String category;
    public final String measurement;

    public CatMeasureJoin(String category, String measurement) {
        this.category = category;
        this.measurement = measurement;
    }
}
