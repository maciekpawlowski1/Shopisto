package com.pawlowski.shopisto.di.app;

import android.app.Application;
import android.content.Context;

import com.pawlowski.shopisto.database.DBHandler;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Application application;

    public AppModule(Application application)
    {
        this.application = application;
    }

    @AppScope
    @Provides
    Context appContext()
    {
        return application;
    }

    @AppScope
    @Provides
    DBHandler dbHandler(Context appContext)
    {
        return new DBHandler(appContext);
    }
}
