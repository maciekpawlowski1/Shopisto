package com.pawlowski.shopisto.di.activity;


import com.pawlowski.shopisto.di.presentation.PresentationComponent;
import com.pawlowski.shopisto.di.presentation.PresentationModule;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    PresentationComponent newPresentationComponent(PresentationModule presentationModule);

}
