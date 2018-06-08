package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

//Define the relationships between objects in both tables
//Note that the layer parent column is for layer IDs not titles since titles are not the primary key,
//so the ID needs to be used to search the hash table for the Layer, just like for searching.
@Entity(tableName = "measure_layer_join",
    primaryKeys = { "measurement", "layer" },
    foreignKeys = {
        @ForeignKey(entity = Measurement.class,
            parentColumns = "name", //parent columns refer to the primary key column for the models
            childColumns = "measurement"),
        @ForeignKey(entity = Layer.class,
            parentColumns = "identifier",
            childColumns = "layer")},
    indices = {
        @Index(value="measurement"),
        @Index(value="layer")
    }
)
public class MeasureLayerJoin {
    @NonNull public final String measurement;
    @NonNull public final String layer;

    public MeasureLayerJoin(@NonNull String measurement, @NonNull String layer) {
        this.measurement = measurement;
        this.layer = layer;
    }
}
