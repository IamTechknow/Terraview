package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Category {
    @PrimaryKey
    private final String name;

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
