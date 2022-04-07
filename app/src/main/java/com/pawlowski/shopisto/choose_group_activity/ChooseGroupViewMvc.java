package com.pawlowski.shopisto.choose_group_activity;

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

public class ChooseGroupViewMvc extends BaseObservableViewMvc<ChooseGroupViewMvc.ChooseGroupButtonsClickListener> {


    private final RecyclerView recyclerView;
    private final ImageView image;
    private final TextView text;
    private final FloatingActionButton button;

    public ChooseGroupViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_choose_group, viewGroup, false);
        recyclerView = findViewById(R.id.recycler_choose_groups);
        image = findViewById(R.id.image_choose_group_activity);
        text = findViewById(R.id.text_choose_group_activity);
        button = findViewById(R.id.add_button_choose_group_activity);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ChooseGroupButtonsClickListener l:listeners)
                {
                    l.onAddGroupClickListener();
                }
            }
        });

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ChooseGroupButtonsClickListener l:listeners)
                {
                    l.onAddGroupClickListener();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ChooseGroupButtonsClickListener l:listeners)
                {
                    l.onAddGroupClickListener();
                }
            }
        });
    }

    public void setRecyclerAdapter(ProductGroupsAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void hideNoGroupsImages()
    {
        image.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
    }

    public void showNoGroupsImages()
    {
        image.setVisibility(View.VISIBLE);
        text.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
    }

    interface ChooseGroupButtonsClickListener
    {
        void onAddGroupClickListener();
    }
}
