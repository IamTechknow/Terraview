package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.CategoryAdapter;
import com.iamtechknow.terraview.model.EventCategory;
import com.iamtechknow.terraview.model.EventCategoryList;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CategoryViewImpl extends Fragment {
    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;

    //Default and empty views
    private RecyclerView mRecyclerView;
    private View empty_view;

    //ViewModel subscription
    private Disposable dataSub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this, new EventsViewModelFactory()).get(CategoryViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Utils.isOnline(getActivity())) {
            mRecyclerView.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);

            //Load the data from the ViewModel
            dataSub = viewModel.loadCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCategoriesLoaded);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataSub.dispose();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        empty_view = rootView.findViewById(R.id.empty_view);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CategoryAdapter(viewModel);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    private void insertList(ArrayList<EventCategory> list) {
        empty_view.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        adapter.insertList(list);
    }

    private void onCategoriesLoaded(EventCategoryList data) {
        data.list.add(0, EventCategory.getAll()); //Add missing "all" category
        insertList(data.list);
    }
}
