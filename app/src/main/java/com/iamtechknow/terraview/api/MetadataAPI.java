package com.iamtechknow.terraview.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

//Given a source and metadata, return the metadata displayed in Worldview when choosing a layer
//foo.fetchdata(modis, CorrectedReflectance) should yield /config/metadata/modis/CorrectedReflectance.html
public interface MetadataAPI {
    @GET("/config/metadata/{source}/{metadata}.html")
    Call<ResponseBody> fetchData(@Path(value = "source", encoded = true) String source, @Path("metadata") String meta);
}
