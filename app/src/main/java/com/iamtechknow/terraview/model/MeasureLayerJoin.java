package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

//Define the relationships between objects in both tables
@Entity(tableName = "measure_layer_join",
    primaryKeys = { "measure", "layer" },
    foreignKeys = {
        @ForeignKey(entity = Measurement.class,
            parentColumns = "name", //parent columns refer to the primary key column for the models
            childColumns = "measurement"),
        @ForeignKey(entity = Layer.class,
            parentColumns = "identifier",
            childColumns = "layer")
})
public class MeasureLayerJoin {
    public final String measurement;
    public final String layer;

    public MeasureLayerJoin(String measurement, String layer) {
        this.measurement = measurement;
        this.layer = layer;
    }
}
