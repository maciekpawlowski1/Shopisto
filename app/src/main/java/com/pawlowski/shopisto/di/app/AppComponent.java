package com.pawlowski.shopisto.di.app;

import com.pawlowski.shopisto.di.activity.ActivityComponent;
import com.pawlowski.shopisto.di.activity.ActivityModule;

import dagger.Component;

@AppScope
@Component(modules = {AppModule.class})
public interface AppComponent {

    ActivityComponent newActivityComponent(ActivityModule activityModule);

}
