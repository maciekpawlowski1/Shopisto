package com.pawlowski.shopisto.main.shopping_lists_fragment;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.models.ListModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ShoppingListsFragmentItemViewMvc extends BaseObservableViewMvc<ShoppingListsFragmentItemViewMvc.ShoppingListsFragmentItemButtonsClickListener> {
    private final TextView listTittleText;
    private final TextView productsReadyText;
    private final ProgressBar progressBar;
    private final ConstraintLayout constraintLayout;
    private final ImageButton moreButton;
    private final EditText editTittle;
    private final ImageButton finishEditingButton;

    private ListModel currentList;
    private int currentPosition;

    public ShoppingListsFragmentItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.shopping_list_card, viewGroup, false);
        listTittleText = findViewById(R.id.list_tittle_card);
        productsReadyText = findViewById(R.id.products_ready_card);
        progressBar = findViewById(R.id.progress_bar_card);
        constraintLayout = findViewById(R.id.layout_shopping_list_card);
        moreButton = findViewById(R.id.more_button_shopping_list_card);
        editTittle = findViewById(R.id.edit_tittle_shopping_list_card);
        finishEditingButton = findViewById(R.id.stop_editting_button_shopping_lists);

        constraintLayout.setOnClickListener(v -> {
            for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
            {
                l.onConstraintLayoutClick(currentList);
            }
        });

        finishEditingButton.setOnClickListener(v -> {
            for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
            {
                l.onFinishEditingClick(currentList, currentPosition, ShoppingListsFragmentItemViewMvc.this);
            }

        });

        moreButton.setOnClickListener(v -> displayPopupMenu());

        editTittle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.

                        for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
                        {
                            l.onTypingDone(currentList, currentPosition, ShoppingListsFragmentItemViewMvc.this);
                        }

                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });


    }

    public void bindList(ListModel list, int position)
    {
        this.currentList = list;
        this.currentPosition = position;
        editTittle.setVisibility(View.INVISIBLE);
        editTittle.setText("");
        finishEditingButton.setVisibility(View.INVISIBLE);
        listTittleText.setVisibility(View.VISIBLE);
        listTittleText.setText(list.getTittle());
        productsReadyText.setText(list.getNumberSelected() + "/" + list.getNumberAll());
        if(list.getNumberAll() == 0)
        {
            progressBar.setProgress(0);
        }
        else
        {
            progressBar.setProgress(list.getNumberSelected() * 100 / list.getNumberAll());
        }
    }

    public void displayPopupMenu()
    {
        PopupMenu popupMenu = new PopupMenu(rootView.getContext(), moreButton);
        popupMenu.getMenuInflater().inflate(R.menu.list_modify_pop_up_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.edit_name_pop_up_menu)
                {
                    for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
                    {
                        l.onEditPopUpButtonClick(currentList, currentPosition, ShoppingListsFragmentItemViewMvc.this);
                    }
                }
                else if(item.getItemId() == R.id.copy_pop_up_menu)
                {
                    for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
                    {
                        l.onCopyPopUpButtonClick(currentList, currentPosition, ShoppingListsFragmentItemViewMvc.this);
                    }
                }
                else if(item.getItemId() == R.id.delete_pop_up_menu)
                {
                    for(ShoppingListsFragmentItemButtonsClickListener l:listeners)
                    {
                        l.onDeletePopUpButtonClick(currentList, currentPosition, ShoppingListsFragmentItemViewMvc.this);
                    }
                }

                return true;
            }
        });

        popupMenu.show();

    }

    public void showEditTittleItems()
    {
        listTittleText.setVisibility(View.INVISIBLE);
        editTittle.setText(currentList.getTittle());
        editTittle.setVisibility(View.VISIBLE);
        finishEditingButton.setVisibility(View.VISIBLE);

        editTittle.requestFocus();
        editTittle.setSelection(currentList.getTittle().length());

    }

    public String getEditTittleInputText()
    {
        Editable editable = editTittle.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public void changeClickableOfConstraint(boolean clickable)
    {
        constraintLayout.setClickable(clickable);
    }

    interface ShoppingListsFragmentItemButtonsClickListener {
        void onConstraintLayoutClick(ListModel currentList);
        void onTypingDone(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc);
        void onFinishEditingClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc);
        void onEditPopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc);
        void onDeletePopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc);
        void onCopyPopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc);
    }
}
