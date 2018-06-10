package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModel;

public class PickerViewModel extends ViewModel {
    private String category, measurement;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }
}
