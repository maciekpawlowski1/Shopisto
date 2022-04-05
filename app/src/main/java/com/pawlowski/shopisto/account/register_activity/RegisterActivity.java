package com.pawlowski.shopisto.account.register_activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.add_friend_activity.AddFriendActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.account.privacy_policy_activity.PrivacyPolicyActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;

public class RegisterActivity extends BaseActivity implements RegisterViewMvc.RegisterButtonsClickListener {

    private RegisterViewMvc viewMvc;

    public static void launch(Context context)
    {
        Intent i = new Intent(context, RegisterActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new RegisterViewMvc(getLayoutInflater(), null);
        setContentView(viewMvc.getRootView());



        if(savedInstanceState != null)
        {
            viewMvc.bindInputTexts(savedInstanceState.getString("mail"),
                    savedInstanceState.getString("password1"),
                    savedInstanceState.getString("password2"));
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mail", viewMvc.getMailInputText());
        outState.putString("password1", viewMvc.getPasswordInputText());
        outState.putString("password2", viewMvc.getRepeatPasswordInputText());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        viewMvc.bindInputTexts(savedInstanceState.getString("mail"),
                savedInstanceState.getString("password1"),
                savedInstanceState.getString("password2"));

    }

    private void createAccount(String userMail, String password)
    {
        String mail = userMail.trim();
        showProgressDialog(getString(R.string.please_wait));
        viewMvc.changeClickableOfButton(false);
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
                viewMvc.changeClickableOfButton(true);
            }
        });
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onRegisterClick() {
        String mail = viewMvc.getMailInputText();
        String password = viewMvc.getPasswordInputText();
        String password2 = viewMvc.getRepeatPasswordInputText();
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

    @Override
    public void onPolicyClick() {
        PrivacyPolicyActivity.launch(this);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}