package com.pawlowski.shopisto.main;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;


public class RestoreFragment extends Fragment {

    RecyclerView recycler;
    RestoreAdapter adapter;

    ConstraintLayout nothingInTrashConstraint;
    MainActivity activity;

    public RestoreFragment() {
        // Required empty public constructor
    }

    public RestoreFragment(MainActivity activity) {
        // Required empty public constructor
        this.activity = activity;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_restore, container, false);

        nothingInTrashConstraint = view.findViewById(R.id.no_lists_in_trash_constraint_trash);
        nothingInTrashConstraint.setVisibility(View.GONE);

        adapter = new RestoreAdapter(getActivity(), this, activity.isOfflineModeOn());

        recycler = view.findViewById(R.id.recycler_restore_fragment);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        activity.getSupportActionBar().setTitle(R.string.trash);

        return view;
    }

    public void showNothingInTrashImage()
    {
        nothingInTrashConstraint.setVisibility(View.VISIBLE);
    }

    public void hideNothingInTrashImage()
    {
        nothingInTrashConstraint.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.setListsAndGroups(DBHandler.getInstance(getActivity().getApplicationContext()).getListsAndGroupsFromTrash());

        if(adapter.getItemCount() == 0)
        {
            showNothingInTrashImage();
        }
        else
        {
            hideNothingInTrashImage();
        }
    }
}