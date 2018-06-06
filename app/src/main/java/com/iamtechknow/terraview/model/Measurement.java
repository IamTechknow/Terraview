package com.iamtechknow.terraview.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

//Define a measurement to be related to a Category (one to many relationship, category has many measurements)
@Entity(foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "categoryName", childColumns = "category", onDelete = CASCADE), tableName = "measurement")
public class Measurement {
    @PrimaryKey
    @ColumnInfo(name = "name")
    private final String name;

    @ColumnInfo(name = "value")
    private final String category;

    public Measurement(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
