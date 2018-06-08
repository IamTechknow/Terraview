package com.iamtechknow.terraview.api;

import com.iamtechknow.terraview.model.EventCategoryList;

import io.reactivex.Single;
import retrofit2.http.GET;

/**
 * API call to parse categories. This is simple enough that Gson annotations are used
 */
public interface CategoryAPI {
    @GET("/api/v2.1/categories")
    Single<EventCategoryList> fetchCategories();
}
