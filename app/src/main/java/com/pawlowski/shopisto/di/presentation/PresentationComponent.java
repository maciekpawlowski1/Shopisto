package com.pawlowski.shopisto.di.presentation;

import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.di.ViewMvcFactory;

import dagger.Subcomponent;

@PresentationScope
@Subcomponent(modules = {PresentationModule.class})
public interface PresentationComponent {

    ViewMvcFactory viewMvcFactory();
    void inject(LoginActivity loginActivity);
}
