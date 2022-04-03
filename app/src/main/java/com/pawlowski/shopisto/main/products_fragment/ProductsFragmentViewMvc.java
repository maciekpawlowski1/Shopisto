package com.pawlowski.shopisto.main.products_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.main.ProductGroupsAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsFragmentViewMvc extends BaseObservableViewMvc<ProductsFragmentViewMvc.ProductsFragmentButtonsClickListener> {

    private final RecyclerView recyclerView;
    private final FloatingActionButton addButton;
    private final ImageView noGroupsImage;
    private final TextView noGroupsText;

    ProductsFragmentViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.fragment_products, viewGroup, false);
        recyclerView = findViewById(R.id.recycler_groups);
        addButton = findViewById(R.id.add_button_groups);
        noGroupsImage = findViewById(R.id.no_groups_image_groups);
        noGroupsText = findViewById(R.id.no_groups_text_groups);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ProductsFragmentButtonsClickListener l:listeners)
                {
                    l.onAddButtonClick();
                }
            }
        });

        noGroupsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ProductsFragmentButtonsClickListener l:listeners)
                {
                    l.onNoGroupsItemClick();
                }
            }
        });
    }

    public void setRecyclerAdapter(ProductGroupsAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void showNoGroupsItems()
    {
        noGroupsText.setVisibility(View.VISIBLE);
        noGroupsImage.setVisibility(View.VISIBLE);
    }

    public void hideNoGroupsItems()
    {
        noGroupsText.setVisibility(View.GONE);
        noGroupsImage.setVisibility(View.GONE);
    }

    public void scrollToRecyclerTop()
    {
        recyclerView.scrollToPosition(0);
    }

    interface ProductsFragmentButtonsClickListener {
        void onAddButtonClick();
        void onNoGroupsItemClick();
    }
}
