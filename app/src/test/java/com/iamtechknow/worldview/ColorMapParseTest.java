package com.iamtechknow.worldview;

import com.iamtechknow.worldview.api.ColorMapAPI;
import com.iamtechknow.worldview.model.ColorMap;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ColorMapParseTest {
    @Test
    public void test() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://gibs.earthdata.nasa.gov")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ColorMapAPI api = retrofit.create(ColorMapAPI.class);
        final Call<ColorMap> map = api.fetchData("MODIS_Combined_Value_Added_AOD");

        try {
            Response<ColorMap> result = map.execute();
            if(result.errorBody() != null)
                System.out.println(result.errorBody());
            else
                System.out.println(result.body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
