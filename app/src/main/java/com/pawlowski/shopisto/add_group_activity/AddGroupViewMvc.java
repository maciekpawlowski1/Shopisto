package com.pawlowski.shopisto.add_group_activity;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddGroupViewMvc extends BaseObservableViewMvc<AddGroupViewMvc.AddGroupButtonsClickListener> {

    private final TextInputEditText tittleInput;
    private final Button createButton;

    AddGroupViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_add_group, viewGroup, false);
        tittleInput = findViewById(R.id.tittle_input_group_creating);
        createButton = findViewById(R.id.create_button_group_creating);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddGroupButtonsClickListener l:listeners)
                {
                    l.onCreateButtonClick();
                }
            }
        });
    }

    public String getTittleInputText()
    {
        Editable editable = tittleInput.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }


    interface AddGroupButtonsClickListener
    {
        void onCreateButtonClick();
    }
}
