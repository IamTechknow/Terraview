package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Category {
    @PrimaryKey
    @NonNull
    private final String name;

    public Category(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public static Category getAllCategory() {
        return new Category("All");
    }
}
