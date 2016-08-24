package com.iamtechknow.worldview.api;

import com.iamtechknow.worldview.model.ColorMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ColorMapAPI {
    @GET("/colormaps/v1.0/{identifier}.xml")
    Call<ColorMap> fetchData(@Path("identifier") String id);
}
