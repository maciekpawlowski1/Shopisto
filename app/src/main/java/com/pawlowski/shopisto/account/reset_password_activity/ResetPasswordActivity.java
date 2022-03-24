package com.pawlowski.shopisto.account.reset_password_activity;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.pawlowski.shopisto.BaseActivity;
import com.pawlowski.shopisto.R;

public class ResetPasswordActivity extends BaseActivity implements ResetPasswordViewMvc.ResetPasswordButtonsClickListener {

    private ResetPasswordViewMvc viewMvc;
    private CountDownTimer backTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new ResetPasswordViewMvc(getLayoutInflater(), null);
        viewMvc.registerListener(this);
        setContentView(viewMvc.getRootView());

    }

    void sendResetMail(String userMail)
    {
        String mail = userMail.trim();
        viewMvc.changeClickableOfResetButton(false);
        showProgressDialog(getString(R.string.please_wait));
        FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                hideProgressDialog();
                showErrorSnackbar(getString(R.string.email_sent), false);
                backTimer = new CountDownTimer(2000, 2000)
                {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        onBackPressed();
                    }
                }.start();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                showErrorSnackbar(getString(R.string.email_sending_failure), true);
                viewMvc.changeClickableOfResetButton(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(backTimer != null)
            backTimer.cancel();
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onResetButtonClick() {
        String mail = viewMvc.getMailInputText();
        if(mail.length() >= 3)
        {
            sendResetMail(mail);
        }
        else
        {
            showErrorSnackbar(getString(R.string.short_mail), true);
        }
    }
}