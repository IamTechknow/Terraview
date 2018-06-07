package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


//Relationships with categories and layers are defined in the Join models and DAOs
@Entity
public class Measurement {
    @PrimaryKey
    private final String name;

    public Measurement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
