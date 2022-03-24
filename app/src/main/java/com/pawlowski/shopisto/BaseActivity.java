package com.pawlowski.shopisto;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    Dialog progresDialog;

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
        progresDialog = new Dialog(this);
        progresDialog.setContentView(R.layout.progress_dialog_layout);
        ((TextView)progresDialog.findViewById(R.id.progressText)).setText(text);
        progresDialog.setCancelable(false);
        progresDialog.setCanceledOnTouchOutside(false);
        progresDialog.show();
    }

    public void hideProgressDialog()
    {
        if(progresDialog != null)
        {
            progresDialog.dismiss();
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
