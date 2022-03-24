package com.pawlowski.shopisto.list_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ListActivityViewMvc extends BaseObservableViewMvc<ListActivityViewMvc.ListActivityButtonsClickListener> {

    private final FloatingActionButton addButton;
    private final FloatingActionButton addGroupButton;
    private final FloatingActionButton addProductsButton;
    private final LinearLayout linearButtonLayout;
    private final TextView textEmptyList;
    private final ImageView imageEmptyList;

    private final AdView mAdView;
    private final SwipeRefreshLayout swipeRefreshLayout;
    private final RecyclerView recyclerView;

    private boolean buttonsVisibility = false;

    ListActivityViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_list, viewGroup, false);
        addButton = findViewById(R.id.add_button_list);
        addGroupButton = findViewById(R.id.add_group_button_list);
        addProductsButton = findViewById(R.id.add_products_button_list);
        linearButtonLayout = findViewById(R.id.linear_layout_buttons_list);
        swipeRefreshLayout = findViewById(R.id.refresh_layout_list_activity);
        recyclerView = findViewById(R.id.recycler_list);
        textEmptyList = findViewById(R.id.text_empty_list);
        imageEmptyList = findViewById(R.id.image_view_empty_list);
        mAdView = findViewById(R.id.ad_view_list_activity);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onAddButtonClick();
                }
            }
        });

        addButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onAddButtonLongClick();
                }
                return true;
            }
        });

        addProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onAddProductsButtonClick();
                }
            }
        });

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onAddGroupButtonClick();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onSwipeRefresh();
                }
            }
        });

        imageEmptyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ListActivityButtonsClickListener l:listeners)
                {
                    l.onEmptyListImageClick();
                }
            }
        });
    }

    public void setRecyclerAdapter(ListAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void stopRefreshing()
    {
        if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    public void showButtons()
    {
        linearButtonLayout.setVisibility(View.VISIBLE);
        linearButtonLayout.startAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.buttons_show_animation));
        buttonsVisibility = true;
        addButton.setImageResource(R.drawable.cancel_icon2);
    }

    public void hideButtons()
    {
        linearButtonLayout.startAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.buttons_hide_animation));
        linearButtonLayout.setVisibility(View.GONE);
        buttonsVisibility = false;
        addButton.setImageResource(R.drawable.plus_icon);
    }

    public void showEmptyListItems()
    {
        textEmptyList.setVisibility(View.VISIBLE);
        imageEmptyList.setVisibility(View.VISIBLE);
    }

    public void hideEmptyListItems()
    {
        textEmptyList.setVisibility(View.GONE);
        imageEmptyList.setVisibility(View.GONE);
    }

    public void showNoProductsImage()
    {
        textEmptyList.setVisibility(View.VISIBLE);
        imageEmptyList.setVisibility(View.VISIBLE);
    }

    public void hideNoProductsImage()
    {
        textEmptyList.setVisibility(View.GONE);
        imageEmptyList.setVisibility(View.GONE);
    }

    public void loadAd(AdRequest adRequest)
    {
        mAdView.loadAd(adRequest);
    }

    public void showOrHideButtons()
    {
        if(!buttonsVisibility)
        {
            showButtons();
        }
        else
        {
            hideButtons();
        }
    }

    public boolean areButtonsVisible()
    {
        return buttonsVisibility;
    }

    interface ListActivityButtonsClickListener
    {
        void onAddButtonClick();
        void onAddButtonLongClick();
        void onAddProductsButtonClick();
        void onAddGroupButtonClick();
        void onSwipeRefresh();
        void onEmptyListImageClick();
    }
}
