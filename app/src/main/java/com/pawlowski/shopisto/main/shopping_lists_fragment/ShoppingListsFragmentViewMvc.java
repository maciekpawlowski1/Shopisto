package com.pawlowski.shopisto.main.shopping_lists_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ShoppingListsFragmentViewMvc extends BaseObservableViewMvc<ShoppingListsFragmentViewMvc.ShoppingListsFragmentButtonsClickListener> {

    private final RecyclerView recyclerView;
    private final FloatingActionButton addButton;
    private final ImageView noListImageView;
    private final TextView noListTextView;
    private final SwipeRefreshLayout swipeRefreshLayout;


    ShoppingListsFragmentViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.fragment_shopping_lists, viewGroup, false);
        recyclerView = findViewById(R.id.recycler_shopping_lists);
        noListImageView = findViewById(R.id.no_list_image_shopping_lists);
        noListTextView = findViewById(R.id.no_list_text_shopping_lists);
        swipeRefreshLayout = findViewById(R.id.refresh_layout_shopping_lists);
        addButton = findViewById(R.id.add_button_shopping_lists);

        addButton.setOnClickListener(v -> {
            for(ShoppingListsFragmentButtonsClickListener l:listeners)
            {
                l.onAddButtonClick();
            }
        });

        noListImageView.setOnClickListener(v -> {
            for(ShoppingListsFragmentButtonsClickListener l:listeners)
            {
                l.onNoListItemClick();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            for(ShoppingListsFragmentButtonsClickListener l:listeners)
            {
                l.onSwipeRefresh();
            }
        });

    }

    public void setRecyclerAdapter(ShoppingListsAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void stopRefreshing()
    {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    public void scrollToRecyclerTop()
    {
        recyclerView.scrollToPosition(0);
    }

    public void changeVisibilityOfNoListItem(boolean isVisible)
    {
        if(isVisible)
        {
            noListTextView.setVisibility(View.VISIBLE);
            noListImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            noListTextView.setVisibility(View.GONE);
            noListImageView.setVisibility(View.GONE);
        }
    }

    interface ShoppingListsFragmentButtonsClickListener {
        void onAddButtonClick();
        void onNoListItemClick();
        void onSwipeRefresh();
    }
}
