package com.pawlowski.shopisto.account.login_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.account.BaseLoginActivity;
import com.pawlowski.shopisto.account.register_activity.RegisterActivity;
import com.pawlowski.shopisto.account.reset_password_activity.ResetPasswordActivity;
import com.pawlowski.shopisto.add_friend_activity.AddFriendActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.main.MainActivity;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class LoginActivity extends BaseLoginActivity implements LoginViewMvc.LoginButtonsClickListener {
    private LoginViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context)
    {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newLoginViewMvcInstance(null);
        viewMvc.registerListener(this);
        setContentView(viewMvc.getRootView());
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            MainActivity.resetListsTimestamp(LoginActivity.this);
            initGoogleLogin();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            MainActivity.launch(this);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

    }

    public void turnOnOfflineModeAction()
    {
        turnOnOfflineMode();
        MainActivity.launch(this);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }



    @Override
    protected void onGoogleAuthComplete(@NonNull Task<AuthResult> task) {
        hideProgressDialog();
        if (task.isSuccessful() && task.getResult().getUser() != null) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success");
            FirebaseUser user = task.getResult().getUser();
            FirebaseDatabase.getInstance().goOnline();
            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                    .child("mail").setValue(user.getEmail());

            startMainActivityAfterLogin();

        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "signInWithCredential:failure", task.getException());
            try {
                throw task.getException();
            }
            catch (FirebaseAuthUserCollisionException collisionException)
            {
                showErrorSnackbar(getString(R.string.mail_already_in_use), true);
            } catch (Exception e) {
                showErrorSnackbar(getString(R.string.login_failed), true);
            }
        }
    }

    @Override
    protected void onSignInWithPasswordComplete(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful() && task.getResult().getUser() != null)
        {
            AuthResult authResult = task.getResult();
            FirebaseDatabase.getInstance().goOnline();
            FirebaseDatabase.getInstance().getReference().child("users").child(authResult.getUser().getUid())
                    .child("mail").setValue(authResult.getUser().getEmail());


            hideProgressDialog();
            startMainActivityAfterLogin();
        }
        else
        {
            hideProgressDialog();
            showErrorSnackbar(getString(R.string.wrong_sign_in), true);
            viewMvc.changeClickableOfSignInButton(true);
        }
    }



    void startMainActivityAfterLogin()
    {
        showErrorSnackbar(getString(R.string.sign_in_success), false);
        if(isOfflineModeOn())
        {
            turnOffOfflineMode();
            OnlineDBHandler.saveAfterOfflineMode(dbHandler);
        }
        else
        {
            MainActivity.launch(this);
        }
        finish();
    }

    @Override
    public void onLoginClick() {
        String email = viewMvc.getMailInputText();
        String password = viewMvc.getPasswordInputText();
        if(email.length() >= 3 && password.length() >= 5 && AddFriendActivity.isMailValid(email))
        {
            //Log in
            showProgressDialog(getString(R.string.please_wait));
            viewMvc.changeClickableOfSignInButton(false);
            logInWithPassword(email, password);
        }
        else
        {
            showErrorSnackbar(getString(R.string.first_fill_email_and_password), true);
        }
    }

    @Override
    public void onCreateAccountClick() {
        RegisterActivity.launch(this);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onOfflineModeClick() {
        turnOnOfflineModeAction();
    }

    @Override
    public void onSignInByGoogleClick() {
        signInByGoogle();
    }

    @Override
    public void onResetPasswordClick() {
        ResetPasswordActivity.launch(this);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}