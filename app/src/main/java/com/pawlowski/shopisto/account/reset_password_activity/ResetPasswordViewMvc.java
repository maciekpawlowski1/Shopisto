package com.pawlowski.shopisto.account.reset_password_activity;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ResetPasswordViewMvc extends BaseObservableViewMvc<ResetPasswordViewMvc.ResetPasswordButtonsClickListener> {

    private final ImageButton backButton;
    private final Button resetButton;
    private final TextInputEditText mailInput;

    public ResetPasswordViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_reset_password, viewGroup, false);
        backButton = findViewById(R.id.back_button_reset);
        resetButton = findViewById(R.id.reset_button_reset);
        mailInput = findViewById(R.id.email_input_reset);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ResetPasswordButtonsClickListener l:listeners)
                {
                    l.onBackClick();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ResetPasswordButtonsClickListener l:listeners)
                {
                    l.onResetButtonClick();
                }
            }
        });
    }

    public String getMailInputText()
    {
        Editable editable = mailInput.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public void changeClickableOfResetButton(boolean isClickable)
    {
        resetButton.setClickable(isClickable);
    }

    interface ResetPasswordButtonsClickListener
    {
        void onBackClick();
        void onResetButtonClick();
    }
}
