package com.pawlowski.shopisto.account;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RegisterViewMvc extends BaseObservableViewMvc<RegisterViewMvc.RegisterButtonsClickListener> {
    private final ImageButton backButton;
    private final Button registerButton;
    private final TextInputEditText mailInput;
    private final TextInputEditText passwordInput;
    private final TextInputEditText passwordInput2;
    private final TextView policyAgreeText;


    public RegisterViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        rootView = layoutInflater.inflate(R.layout.activity_register, viewGroup, false);
        backButton = findViewById(R.id.back_button_register);
        registerButton = findViewById(R.id.register_button_register);
        mailInput = findViewById(R.id.email_input_register);
        passwordInput = findViewById(R.id.password_input_register);
        passwordInput2 = findViewById(R.id.password_repeat_input_register);
        policyAgreeText = findViewById(R.id.agree_for_privacy_text_register);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(RegisterButtonsClickListener l:listeners)
                {
                    l.onBackClick();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(RegisterButtonsClickListener l:listeners)
                {
                    l.onRegisterClick();
                }
            }
        });

        policyAgreeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(RegisterButtonsClickListener l:listeners)
                {
                    l.onPolicyClick();
                }
            }
        });
    }

    public void bindInputTexts(String mail, String password, String repeatedPassword)
    {
        mailInput.setText(mail);
        passwordInput.setText(password);
        passwordInput2.setText(repeatedPassword);
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

    public String getPasswordInputText()
    {
        Editable editable = passwordInput.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public String getRepeatPasswordInputText()
    {
        Editable editable = passwordInput2.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public void changeClickableOfButton(boolean isClickable)
    {
        registerButton.setClickable(isClickable);
    }

    interface RegisterButtonsClickListener
    {
        void onBackClick();
        void onRegisterClick();
        void onPolicyClick();
    }
}
