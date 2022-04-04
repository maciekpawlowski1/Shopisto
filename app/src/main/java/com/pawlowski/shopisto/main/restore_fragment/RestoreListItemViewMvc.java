package com.pawlowski.shopisto.main.restore_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.models.ListModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RestoreListItemViewMvc extends BaseRestoreItemViewMvc<RestoreListItemViewMvc.RestoreListItemButtonsClickListener, ListModel> {
    private final TextView listTittleText;
    private final TextView progressText;
    private final ProgressBar progressBar;
    private final Button undoButton;
    private final Button deleteButton;

    private ListModel currentList;
    private int currentPosition;

    RestoreListItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.restore_list_card, viewGroup, false);
        listTittleText = findViewById(R.id.tittle_restore_list_card);
        progressText = findViewById(R.id.text_progress_restore_list_card);
        progressBar = findViewById(R.id.progress_bar_restore_list_card);
        undoButton = findViewById(R.id.undo_button_restore_list_card);
        deleteButton = findViewById(R.id.delete_button_restore_list_card);

        undoButton.setOnClickListener(v -> {
            for(RestoreListItemButtonsClickListener l:listeners)
            {
                l.onListUndoClick(currentList, currentPosition);
            }
        });

        deleteButton.setOnClickListener(v -> {
            for(RestoreListItemButtonsClickListener l:listeners)
            {
                l.onListDeleteClick(currentList, currentPosition);
            }
        });
    }

    @Override
    public void bindItem(ListModel currentList, int position) {
        this.currentList = currentList;
        this.currentPosition = position;

        listTittleText.setText(currentList.getTittle());

        if(currentList.getNumberAll() != 0)
        {
            progressBar.setProgress(100*currentList.getNumberSelected()/currentList.getNumberAll());
        }
        else
        {
            progressBar.setProgress(0);
        }
        progressText.setText(currentList.getNumberSelected()+"/"+currentList.getNumberAll());
    }

    interface RestoreListItemButtonsClickListener {
        void onListUndoClick(ListModel currentList, int currentPosition);
        void onListDeleteClick(ListModel currentList, int currentPosition);
    }
}
