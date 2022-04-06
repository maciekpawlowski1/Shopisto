package com.pawlowski.shopisto.di;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pawlowski.shopisto.account.login_activity.LoginViewMvc;
import com.pawlowski.shopisto.account.register_activity.RegisterViewMvc;

import javax.inject.Inject;

public class ViewMvcFactory {

    private LayoutInflater layoutInflater;
    @Inject
    public ViewMvcFactory(LayoutInflater inflater) {
        this.layoutInflater = inflater;
    }

    public LoginViewMvc newLoginViewMvcInstance(ViewGroup viewGroup)
    {
        return new LoginViewMvc(layoutInflater, viewGroup);
    }


    public RegisterViewMvc newRegisterViewMvcInstance(ViewGroup viewGroup)
    {
        return new RegisterViewMvc(layoutInflater, viewGroup);
    }

}
