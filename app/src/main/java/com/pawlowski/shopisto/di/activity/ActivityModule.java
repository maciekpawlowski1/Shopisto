package com.pawlowski.shopisto.di.activity;

import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final AppCompatActivity activity;

    public ActivityModule(AppCompatActivity activity)
    {
        this.activity = activity;
    }

    @ActivityScope
    @Provides
    LayoutInflater layoutInflater()
    {
        return activity.getLayoutInflater();
    }
}
