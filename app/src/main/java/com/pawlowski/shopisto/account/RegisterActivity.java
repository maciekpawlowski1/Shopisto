package com.pawlowski.shopisto.account;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.AddFriendActivity;
import com.pawlowski.shopisto.BaseActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.main.MainActivity;

public class RegisterActivity extends BaseActivity {
    ImageButton backButton;
    Button registerButton;
    TextInputEditText mailInput;
    TextInputEditText passwordInput;
    TextInputEditText passwordInput2;
    TextView policyAgreeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        backButton = findViewById(R.id.back_button_register);
        registerButton = findViewById(R.id.register_button_register);
        mailInput = findViewById(R.id.email_input_register);
        passwordInput = findViewById(R.id.password_input_register);
        passwordInput2 = findViewById(R.id.password_repeat_input_register);


        if(savedInstanceState != null)
        {
            mailInput.setText(savedInstanceState.getString("mail"));
            passwordInput.setText(savedInstanceState.getString("password1"));
            passwordInput2.setText(savedInstanceState.getString("password2"));
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String password2 = passwordInput2.getText().toString();
                if(mail.length() < 3 || !AddFriendActivity.isMailValid(mail))
                {
                    showErrorSnackbar(getString(R.string.invalid_mail), true);
                }
                else if (password.length() < 6)
                {
                    showErrorSnackbar(getString(R.string.short_password), true);
                }
                else if(!password.equals(password2))
                {
                    showErrorSnackbar(getString(R.string.different_passwords), true);
                }
                else
                {
                    //Create account
                    createAccount(mail, password);

                }
            }
        });

        policyAgreeText = findViewById(R.id.agree_for_privacy_text_register);
        policyAgreeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, PrivacyPolicyActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mail", mailInput.getText().toString());
        outState.putString("password1", passwordInput.getText().toString());
        outState.putString("password2", passwordInput2.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mailInput.setText(savedInstanceState.getString("mail"));
        passwordInput.setText(savedInstanceState.getString("password1"));
        passwordInput2.setText(savedInstanceState.getString("password2"));

    }

    void createAccount(String userMail, String password)
    {
        String mail = userMail.trim();
        showProgressDialog(getString(R.string.please_wait));
        registerButton.setClickable(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                if(isOfflineModeOn())
                {
                    turnOffOfflineMode();
                    OnlineDBHandler.saveAfterOfflineMode(DBHandler.getInstance(getApplicationContext()));
                }

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                //firebaseDatabase.setPersistenceEnabled(true);


                firebaseDatabase.getReference().child("users").child(authResult.getUser().getUid())
                        .child("mail").setValue(authResult.getUser().getEmail());

                hideProgressDialog();
                showErrorSnackbar(getString(R.string.account_created), false);
                //FirebaseAuth.getInstance().signOut();

                new CountDownTimer(2000, 2000)
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
                showErrorSnackbar(getString(R.string.account_not_created), true);
                registerButton.setClickable(true);
            }
        });
    }
}