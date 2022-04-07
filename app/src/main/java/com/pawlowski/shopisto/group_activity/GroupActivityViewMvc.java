package com.pawlowski.shopisto.group_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupActivityViewMvc extends BaseObservableViewMvc<GroupActivityViewMvc.GroupActivityButtonsClickListener> {
    private final ImageView noProductsImage;
    private final TextView noProductsText;
    private final AdView mAdView;
    private final RecyclerView recycler;
    private final FloatingActionButton addButton;

    public GroupActivityViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_group, viewGroup, false);
        recycler = findViewById(R.id.recycler_products_in_groups);
        addButton = findViewById(R.id.add_product_button_group);
        noProductsImage = findViewById(R.id.no_products_image_products_in_group);
        noProductsText = findViewById(R.id.no_products_text_products_in_groups);
        mAdView = findViewById(R.id.ad_view_group_activity);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(GroupActivityButtonsClickListener l:listeners)
                {
                    l.onAddButtonClick();
                }
            }
        });

    }

    public void setRecyclerAdapter(ProductsInGroupAdapter adapter)
    {
        recycler.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recycler.setAdapter(adapter);
    }

    public void loadAd(AdRequest adRequest)
    {
        mAdView.loadAd(adRequest);
    }

    public void showNoProductsImage()
    {
        noProductsText.setVisibility(View.VISIBLE);
        noProductsImage.setVisibility(View.VISIBLE);
    }

    public void hideNoProductsImage()
    {
        noProductsText.setVisibility(View.GONE);
        noProductsImage.setVisibility(View.GONE);
    }

    interface GroupActivityButtonsClickListener
    {
        void onAddButtonClick();
    }
}
