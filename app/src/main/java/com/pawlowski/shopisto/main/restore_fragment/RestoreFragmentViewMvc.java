package com.pawlowski.shopisto.main.restore_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RestoreFragmentViewMvc extends BaseObservableViewMvc<RestoreFragmentViewMvc.RestoreFragmentButtonsClickListener> {

    private final RecyclerView recycler;
    private final ConstraintLayout nothingInTrashConstraint;

    public RestoreFragmentViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        rootView = layoutInflater.inflate(R.layout.fragment_restore, viewGroup, false);
        nothingInTrashConstraint = findViewById(R.id.no_lists_in_trash_constraint_trash);
        recycler = findViewById(R.id.recycler_restore_fragment);

        nothingInTrashConstraint.setVisibility(View.GONE);
    }

    public void setRecyclerAdapter(RestoreAdapter adapter)
    {
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
    }

    public void setNothingInTrashConstraintVisibility(boolean visible)
    {
        if(visible)
        {
            nothingInTrashConstraint.setVisibility(View.VISIBLE);
        }
        else
        {
            nothingInTrashConstraint.setVisibility(View.GONE);
        }
    }

    interface RestoreFragmentButtonsClickListener {

    }
}
