package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

//Define the relationships between objects in both tables
@Entity(tableName = "cat_measure_join",
    primaryKeys = { "category", "measurement" },
    foreignKeys = {
        @ForeignKey(entity = Category.class,
            parentColumns = "name", //name refers to the name column in both models
            childColumns = "category"),
        @ForeignKey(entity = Measurement.class,
            parentColumns = "name",
            childColumns = "measurement")},
    indices = {
        @Index(value="category"),
        @Index(value="measurement")
    }
)
public class CatMeasureJoin {
    @NonNull public final String category;
    @NonNull public final String measurement;

    public CatMeasureJoin(@NonNull String category, @NonNull String measurement) {
        this.category = category;
        this.measurement = measurement;
    }
}
