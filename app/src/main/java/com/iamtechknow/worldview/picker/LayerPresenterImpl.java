package com.iamtechknow.worldview.picker;

import com.iamtechknow.worldview.api.MetadataAPI;
import com.iamtechknow.worldview.data.DataSource;
import com.iamtechknow.worldview.model.Layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LayerPresenterImpl implements LayerPresenter {
    private static final String BASE_URL = "https://worldview.earthdata.nasa.gov/";

    private LayerView view;
    private DataSource data;
    private Retrofit retrofit;

    public LayerPresenterImpl(LayerView _view) {
        view = _view;
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
    }

    @Override
    public ArrayList<Layer> getCurrStack() {
        return null;
    }

    @Override
    public void useRetrofit(String description) {
        if(description == null)
            return;

        MetadataAPI api = retrofit.create(MetadataAPI.class);
        String[] temp = description.split("/"); //must split data for URL to work
        final Call<ResponseBody> result = api.fetchData(temp[0], temp[1]);
        Observable.just(true).map(new Func1<Boolean, Response<ResponseBody> >() {
            @Override
            public Response<ResponseBody> call(Boolean aBoolean) {
                Response<ResponseBody> r = null;
                try {
                    r = result.execute();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return r;
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Response<ResponseBody>>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(Response<ResponseBody> r) {
                    try {
                        view.showInfo(r.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    @Override
    public void updateSelectedItems(HashSet<String> set) {

    }

    @Override
    public void setItemChecked(int position, boolean isSelected) {

    }

    @Override
    public boolean isItemChecked(int position) {
        return false;
    }

    @Override
    public Layer searchLayer(int pos) {
        return null;
    }
}
