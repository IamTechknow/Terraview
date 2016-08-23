package com.iamtechknow.worldview;

import com.iamtechknow.worldview.api.ColorMapAPI;
import com.iamtechknow.worldview.model.ColorMap;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
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
            ColorMap result = map.execute().body();
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
