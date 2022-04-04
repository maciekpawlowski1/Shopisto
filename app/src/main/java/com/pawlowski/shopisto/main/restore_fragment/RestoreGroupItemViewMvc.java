package com.pawlowski.shopisto.main.restore_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ListModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RestoreGroupItemViewMvc extends BaseRestoreItemViewMvc<RestoreGroupItemViewMvc.RestoreGroupItemButtonsClickListener, GroupModel> {

    private final TextView groupTittleText;
    private final TextView groupNumberText;
    private final Button groupUndoButton;
    private final Button groupDeleteButton;

    private GroupModel currentGroup;
    private int currentPosition;

    RestoreGroupItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.restore_group_card, viewGroup, false);
        groupDeleteButton = findViewById(R.id.delete_button_restore_group_card);
        groupUndoButton = findViewById(R.id.undo_button_restore_group_card);
        groupTittleText = findViewById(R.id.tittle_restore_group_card);
        groupNumberText = findViewById(R.id.number_restore_group_card);

        groupUndoButton.setOnClickListener(v -> {
            for(RestoreGroupItemButtonsClickListener l:listeners)
            {
                l.onGroupUndoClick(currentGroup, currentPosition);
            }
        });

        groupDeleteButton.setOnClickListener(v -> {
            for(RestoreGroupItemButtonsClickListener l:listeners)
            {
                l.onGroupDeleteClick(currentGroup, currentPosition);
            }
        });
    }

    @Override
    public void bindItem(GroupModel currentGroup, int position) {
        this.currentGroup = currentGroup;
        this.currentPosition = position;

        groupTittleText.setText(currentGroup.getTittle());
        groupNumberText.setText(currentGroup.getProducts().size()+" " + rootView.getContext().getString(R.string.xx_products));
    }

    interface RestoreGroupItemButtonsClickListener {
        void onGroupUndoClick(GroupModel currentGroup, int currentPosition);
        void onGroupDeleteClick(GroupModel currentGroup, int currentPosition);
    }
}
