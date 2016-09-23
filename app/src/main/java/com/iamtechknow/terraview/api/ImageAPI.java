package com.iamtechknow.terraview.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * API declaration to build a request for a GIBS tile image. Works for PNG and JPG formats.
 */
public interface ImageAPI {
    @GET("/wmts/epsg3857/best/{identifier}/default/{date}/{tileMatrixSet}/{zoom}/{y}/{x}.{format}")
    Call<ResponseBody> fetchImage(@Path("identifier") String id, @Path("date") String date, @Path("tileMatrixSet") String matrix,
                                  @Path("zoom") String zoom, @Path("y") String y, @Path("x") String x, @Path("format") String format);
}
