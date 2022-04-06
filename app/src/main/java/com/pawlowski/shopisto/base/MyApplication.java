package com.pawlowski.shopisto.base;

import android.app.Application;

import com.pawlowski.shopisto.di.app.AppComponent;
import com.pawlowski.shopisto.di.app.AppModule;
import com.pawlowski.shopisto.di.app.DaggerAppComponent;

public class MyApplication extends Application {

    private AppComponent appComponent;

    public AppComponent getAppComponent() {
        if(appComponent == null)
        {
            appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        }
        return appComponent;
    }
}
