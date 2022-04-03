package com.pawlowski.shopisto.main.restore_fragment;

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
import com.pawlowski.shopisto.main.MainActivity;


public class RestoreFragment extends Fragment {

    private RestoreAdapter adapter;
    private MainActivity activity;

    private RestoreFragmentViewMvc viewMvc;

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
        viewMvc = new RestoreFragmentViewMvc(inflater, container);

        adapter = new RestoreAdapter(getActivity(), this, activity.isOfflineModeOn());

        viewMvc.setRecyclerAdapter(adapter);

        activity.getSupportActionBar().setTitle(R.string.trash);

        return viewMvc.getRootView();
    }

    public void showNothingInTrashImage()
    {
        viewMvc.setNothingInTrashConstraintVisibility(true);
    }

    public void hideNothingInTrashImage()
    {
        viewMvc.setNothingInTrashConstraintVisibility(false);
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