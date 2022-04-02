package com.pawlowski.shopisto.choose_products_from_group_activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.group_activity.ProductsInGroupAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChooseProductsFromGroupViewMvc extends BaseObservableViewMvc<ChooseProductsFromGroupViewMvc.ChooseProductsFromGroupButtonsClickListener> {

    private final RecyclerView recyclerView;

    public ChooseProductsFromGroupViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        rootView = layoutInflater.inflate(R.layout.activity_choose_products_from_group, viewGroup, false);
        recyclerView = findViewById(R.id.recycler_choose_products_in_groups);

    }

    public void setRecyclerAdapter(ProductsInGroupAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    interface ChooseProductsFromGroupButtonsClickListener
    {

    }
}
