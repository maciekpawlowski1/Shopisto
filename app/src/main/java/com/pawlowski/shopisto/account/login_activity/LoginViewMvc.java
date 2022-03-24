package com.pawlowski.shopisto.account.login_activity;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoginViewMvc extends BaseObservableViewMvc<LoginViewMvc.LoginButtonsClickListener> {
    private final TextView createAccountText;
    private final TextView resetPasswordText;
    private final TextInputEditText mailInput;
    private final TextInputEditText passwordInput;
    private final Button loginButton;
    private final TextView offlineModeText;
    private final SignInButton signInByGoogleButton;

    public LoginViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        rootView = layoutInflater.inflate(R.layout.activity_login, viewGroup, false);
        createAccountText = findViewById(R.id.create_account_login);
        resetPasswordText = findViewById(R.id.reset_password_login);
        mailInput = findViewById(R.id.email_input_login);
        passwordInput = findViewById(R.id.password_input_login);
        loginButton = findViewById(R.id.login_button_login);
        signInByGoogleButton = findViewById(R.id.sign_in_by_google_login);
        offlineModeText = findViewById(R.id.offline_mode_text_login_activity);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(LoginButtonsClickListener l:listeners)
                {
                    l.onLoginClick();
                }
            }
        });

        offlineModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(LoginButtonsClickListener l:listeners)
                {
                    l.onOfflineModeClick();
                }
            }
        });

        signInByGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(LoginButtonsClickListener l:listeners)
                {
                    l.onSignInByGoogleClick();
                }
            }
        });

        resetPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(LoginButtonsClickListener l:listeners)
                {
                    l.onResetPasswordClick();
                }
            }
        });

        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(LoginButtonsClickListener l:listeners)
                {
                    l.onCreateAccountClick();
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

    public void changeClickableOfSignInButton(boolean isClickable)
    {
        signInByGoogleButton.setClickable(isClickable);
    }

    interface LoginButtonsClickListener
    {
        void onLoginClick();
        void onCreateAccountClick();
        void onOfflineModeClick();
        void onSignInByGoogleClick();
        void onResetPasswordClick();
    }
}
