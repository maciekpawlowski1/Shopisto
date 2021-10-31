package com.pawlowski.shopisto.account;

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

public class ResetPasswordActivity extends BaseActivity {

    ImageButton backButton;
    Button resetButton;
    TextInputEditText mailInput;
    CountDownTimer backTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        backButton = findViewById(R.id.back_button_reset);
        resetButton = findViewById(R.id.reset_button_reset);
        mailInput = findViewById(R.id.email_input_reset);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailInput.getText().toString();
                if(mail.length() >= 3)
                {
                    sendResetMail(mail);
                }
                else
                {
                    showErrorSnackbar(getString(R.string.short_mail), true);
                }
            }
        });
    }

    void sendResetMail(String userMail)
    {
        String mail = userMail.trim();
        resetButton.setClickable(false);
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
                resetButton.setClickable(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(backTimer != null)
            backTimer.cancel();
    }
}