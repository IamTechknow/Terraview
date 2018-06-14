package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

//Relationships with categories and layers are defined in the Join models and DAOs
@Entity
public class Measurement implements Comparable<Measurement> {
    @PrimaryKey
    @NonNull
    private final String name;

    public Measurement(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Measurement && name.equals(((Measurement) obj).name);
    }

    @Override
    public int compareTo(@NonNull Measurement o) {
        return name.compareTo(o.name);
    }
}
