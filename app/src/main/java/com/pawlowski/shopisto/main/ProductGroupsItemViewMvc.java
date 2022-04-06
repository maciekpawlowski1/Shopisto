package com.pawlowski.shopisto.main;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.models.GroupModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ProductGroupsItemViewMvc extends BaseObservableViewMvc<ProductGroupsItemViewMvc.ProductGroupsItemButtonsClickListener> {

    private final TextView tittleText;
    private final TextView numberText;
    private final ConstraintLayout constraintLayout;
    private final ImageButton moreButton;
    private final EditText editTittle;
    private final ImageButton finishEditingButton;

    private GroupModel currentGroup;
    private int currentPosition;

    ProductGroupsItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.product_group_card, viewGroup, false);
        tittleText = findViewById(R.id.tittle_group_card);
        numberText = findViewById(R.id.number_group_card);
        constraintLayout = findViewById(R.id.constraint_group_card);
        moreButton = findViewById(R.id.more_button_group_card);
        editTittle = findViewById(R.id.edit_tittle_group_card);
        finishEditingButton = findViewById(R.id.stop_editting_button_group_card);


        editTittle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        String newTittle = editTittle.getText().toString();

                        for(ProductGroupsItemButtonsClickListener l:listeners)
                        {
                            l.onFinishedEditingClick(currentGroup, newTittle, currentPosition, ProductGroupsItemViewMvc.this);
                        }
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });


        finishEditingButton.setOnClickListener(v -> {
            String newTittle = editTittle.getText().toString();
            for(ProductGroupsItemButtonsClickListener l:listeners)
            {
                l.onFinishedEditingClick(currentGroup, newTittle, currentPosition, ProductGroupsItemViewMvc.this);
            }
        });

        constraintLayout.setOnClickListener(v -> {
            for(ProductGroupsItemButtonsClickListener l:listeners)
            {
                l.onConstraintClick(currentGroup);
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPopUpMenu();
            }
        });
    }

    public void hideTittleEditing()
    {
        editTittle.setVisibility(View.INVISIBLE);
        tittleText.setVisibility(View.VISIBLE);
        editTittle.setText("");
        finishEditingButton.setVisibility(View.INVISIBLE);
        constraintLayout.setClickable(true);
    }

    public void showTittleEditing()
    {
        constraintLayout.setClickable(false);
        tittleText.setVisibility(View.INVISIBLE);
        editTittle.setText(currentGroup.getTittle());
        editTittle.setVisibility(View.VISIBLE);
        finishEditingButton.setVisibility(View.VISIBLE);
        editTittle.requestFocus();
        editTittle.setSelection(currentGroup.getTittle().length());
        showKeyboard();
    }

    public void bindGroup(GroupModel currentGroup, boolean choosing, int position)
    {
        this.currentGroup = currentGroup;
        this.currentPosition = position;

        editTittle.setVisibility(View.INVISIBLE);
        tittleText.setVisibility(View.VISIBLE);
        finishEditingButton.setVisibility(View.INVISIBLE);
        editTittle.setText("");
        tittleText.setText(currentGroup.getTittle());
        numberText.setText(currentGroup.getProducts().size()+" " + rootView.getContext().getString(R.string.xx_products));

        if(choosing)
        {
            moreButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            moreButton.setVisibility(View.VISIBLE);
        }
    }



    public void displayPopUpMenu()
    {
        PopupMenu popupMenu = new PopupMenu(rootView.getContext(), moreButton);
        popupMenu.getMenuInflater().inflate(R.menu.list_modify_pop_up_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.edit_name_pop_up_menu)
                {
                    for(ProductGroupsItemButtonsClickListener l:listeners)
                    {
                        l.onEditNamePopUpClick(currentGroup, currentPosition, ProductGroupsItemViewMvc.this);
                    }
                }
                else if(item.getItemId() == R.id.copy_pop_up_menu)
                {
                    for(ProductGroupsItemButtonsClickListener l:listeners)
                    {
                        l.onCopyPopUpClick(currentGroup, currentPosition, ProductGroupsItemViewMvc.this);
                    }
                }
                else if(item.getItemId() == R.id.delete_pop_up_menu)
                {
                    for(ProductGroupsItemButtonsClickListener l:listeners)
                    {
                        l.onDeletePopUpClick(currentGroup, currentPosition, ProductGroupsItemViewMvc.this);
                    }
                }

                return true;
            }
        });
        popupMenu.show();
    }

    interface ProductGroupsItemButtonsClickListener {
        void onConstraintClick(GroupModel currentGroup);
        void onEditNamePopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc);
        void onFinishedEditingClick(GroupModel currentGroup, String tittleInput, int currentPosition, ProductGroupsItemViewMvc viewMvc);
        void onCopyPopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc);
        void onDeletePopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc);
    }
}
