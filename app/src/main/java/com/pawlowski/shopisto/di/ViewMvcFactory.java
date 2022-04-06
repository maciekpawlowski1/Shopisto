package com.pawlowski.shopisto.di;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pawlowski.shopisto.account.login_activity.LoginViewMvc;
import com.pawlowski.shopisto.account.register_activity.RegisterViewMvc;
import com.pawlowski.shopisto.account.reset_password_activity.ResetPasswordViewMvc;
import com.pawlowski.shopisto.add_friend_activity.AddFriendViewMvc;
import com.pawlowski.shopisto.add_group_activity.AddGroupViewMvc;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class ViewMvcFactory {

    private LayoutInflater layoutInflater;
    @Inject
    public ViewMvcFactory(LayoutInflater inflater) {
        this.layoutInflater = inflater;
    }

    public LoginViewMvc newLoginViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new LoginViewMvc(layoutInflater, viewGroup);
    }


    public RegisterViewMvc newRegisterViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new RegisterViewMvc(layoutInflater, viewGroup);
    }

    public ResetPasswordViewMvc newResetPasswordViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new ResetPasswordViewMvc(layoutInflater, viewGroup);
    }

    public AddGroupViewMvc newAddGroupViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new AddGroupViewMvc(layoutInflater, viewGroup);
    }

    public AddFriendViewMvc newAddFriendViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new AddFriendViewMvc(layoutInflater, viewGroup);
    }

}
