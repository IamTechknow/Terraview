package com.iamtechknow.terraview.api;

import com.iamtechknow.terraview.model.CategoryList;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface CategoryAPI {
    @GET("/api/v2.1/categories")
    Observable<CategoryList> fetchCategories();
}
