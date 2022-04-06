package com.pawlowski.shopisto.base;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.di.activity.ActivityComponent;
import com.pawlowski.shopisto.di.activity.ActivityModule;
import com.pawlowski.shopisto.di.presentation.PresentationComponent;
import com.pawlowski.shopisto.di.presentation.PresentationModule;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {

    private ActivityComponent activityComponent;
    private PresentationComponent presentationComponent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public ActivityComponent getActivityComponent() {
        if(activityComponent == null)
        {
            activityComponent = ((MyApplication)getApplication()).getAppComponent().newActivityComponent(new ActivityModule(this));
        }
        return activityComponent;
    }

    public PresentationComponent getPresentationComponent()
    {
        if(presentationComponent == null)
        {
            presentationComponent = getActivityComponent().newPresentationComponent(new PresentationModule());
        }
        return presentationComponent;
    }

    private Dialog progressDialog;

    public void showErrorSnackbar(String text, boolean error)
    {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);

        View snackbarView = snackbar.getView();

        if(error)
        {
            snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }
        else
        {
            snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.zielony));
        }
        snackbar.show();
    }

    public void showProgressDialog(String text)
    {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog_layout);
        ((TextView) progressDialog.findViewById(R.id.progressText)).setText(text);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void hideProgressDialog()
    {
        if(progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }


    public boolean isOfflineModeOn()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("offlineModePreferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isOfflineOn", false);
    }

    public void turnOnOfflineMode()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("offlineModePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isOfflineOn", true);
        editor.commit();
    }

    public void turnOffOfflineMode()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("offlineModePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isOfflineOn", false);
        editor.commit();
    }


}
