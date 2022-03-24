package com.pawlowski.shopisto.account.login_activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.AddFriendActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.account.register_activity.RegisterActivity;
import com.pawlowski.shopisto.account.reset_password_activity.ResetPasswordActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.main.MainActivity;
import com.pawlowski.shopisto.R;

public class LoginActivity extends BaseActivity implements LoginViewMvc.LoginButtonsClickListener {


    private static final int RC_SIGN_IN = 51;
    private static final String TAG = "sign_in_by_google";

    private LoginViewMvc viewMvc;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new LoginViewMvc(getLayoutInflater(), null);
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
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

    }

    public void turnOnOfflineModeAction()
    {
        turnOnOfflineMode();
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    
    private void initGoogleLogin()
    {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void signInByGoogle()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account.getIdToken());
            }
            else
            {
                Log.w(TAG, "Google sign in failed");
                showErrorSnackbar(getString(R.string.login_failed), true);
            }
            /*Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                showErrorSnackbar(getString(R.string.login_failed), true);
            }*/
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        showProgressDialog(getString(R.string.please_wait));
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().goOnline();
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                    .child("mail").setValue(user.getEmail());

                            startMainActivityAfterLogin();

                            //updateUI(user);
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
                                //e.printStackTrace();
                                showErrorSnackbar(getString(R.string.login_failed), true);
                            }


                            //updateUI(null);
                        }
                    }
                });
    }

    void logInWithPassword(String userMail, String password)
    {
        String mail = userMail.trim();
        //String password = userPassword.trim();
        viewMvc.changeClickableOfSignInButton(false);
        showProgressDialog(getString(R.string.please_wait));
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {


                FirebaseDatabase.getInstance().goOnline();
                FirebaseDatabase.getInstance().getReference().child("users").child(authResult.getUser().getUid())
                        .child("mail").setValue(authResult.getUser().getEmail());


                hideProgressDialog();
                startMainActivityAfterLogin();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                showErrorSnackbar(getString(R.string.wrong_sign_in), true);
                viewMvc.changeClickableOfSignInButton(true);
            }
        });

    }

    void startMainActivityAfterLogin()
    {
        showErrorSnackbar(getString(R.string.sign_in_success), false);
        if(isOfflineModeOn())
        {
            turnOffOfflineMode();
            OnlineDBHandler.saveAfterOfflineMode(DBHandler.getInstance(getApplicationContext()));
        }
        else
        {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
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
            //showProgressDialog(getString(R.string.please_wait));
            logInWithPassword(email, password);
            //hideProgressDialog();
        }
        else
        {
            showErrorSnackbar(getString(R.string.first_fill_email_and_password), true);
        }
    }

    @Override
    public void onCreateAccountClick() {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
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
        Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}