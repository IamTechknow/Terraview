package com.iamtechknow.terraview.events;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.CategoryAdapter;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.picker.RxBus;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

public class CategoryViewImpl extends Fragment implements CategoryView {
    private CategoryPresenter presenter;
    private CategoryAdapter adapter;

    //Default and empty views
    private RecyclerView mRecyclerView;
    private View empty_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new CategoryPresenterImpl(RxBus.getInstance(), this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Utils.isOnline(getActivity())) {
            mRecyclerView.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
            presenter.loadCategories();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.clearPresenter();
        presenter.detachView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        empty_view = rootView.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CategoryAdapter(presenter);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void insertList(ArrayList<Category> list) {
        empty_view.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        adapter.insertList(list);
    }
}
