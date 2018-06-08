package com.iamtechknow.terraview.api;

import com.iamtechknow.terraview.model.ColorMap;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ColorMapAPI {
    @GET("/colormaps/v1.0/{identifier}.xml")
    Single<ColorMap> fetchData(@Path("identifier") String id);
}
