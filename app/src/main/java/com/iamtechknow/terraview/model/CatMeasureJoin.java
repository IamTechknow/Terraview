package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

//Define the relationships between objects in both tables
//Due to the possibility of duplicates in the JSON, we need to be able to put them in a hash set.
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
public class CatMeasureJoin implements Comparable<CatMeasureJoin> {
    @NonNull public final String category;
    @NonNull public final String measurement;

    public CatMeasureJoin(@NonNull String category, @NonNull String measurement) {
        this.category = category;
        this.measurement = measurement;
    }

    @Override
    public int hashCode() {
        return category.hashCode() + measurement.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        else if(obj instanceof CatMeasureJoin) {
            CatMeasureJoin that = (CatMeasureJoin) obj;
            return category.equals(that.category) && measurement.equals(that.measurement);
        }
        return false;
    }

    //Since measurements are listed out, we want to sort by measurements
    @Override
    public int compareTo(@NonNull CatMeasureJoin o) {
        return measurement.compareTo(o.measurement);
    }
}
