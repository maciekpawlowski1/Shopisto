package com.pawlowski.shopisto.di;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pawlowski.shopisto.account.login_activity.LoginViewMvc;
import com.pawlowski.shopisto.account.register_activity.RegisterViewMvc;
import com.pawlowski.shopisto.account.reset_password_activity.ResetPasswordViewMvc;
import com.pawlowski.shopisto.add_friend_activity.AddFriendViewMvc;
import com.pawlowski.shopisto.add_group_activity.AddGroupViewMvc;
import com.pawlowski.shopisto.add_products_to_list_activity.AddProductsToListViewMvc;
import com.pawlowski.shopisto.choose_group_activity.ChooseGroupViewMvc;
import com.pawlowski.shopisto.choose_products_from_group_activity.ChooseProductsFromGroupViewMvc;
import com.pawlowski.shopisto.edit_product_activity.EditProductViewMvc;
import com.pawlowski.shopisto.group_activity.GroupActivityViewMvc;
import com.pawlowski.shopisto.list_activity.ListActivityViewMvc;
import com.pawlowski.shopisto.share_activity.ShareActivityViewMvc;

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

    public AddProductsToListViewMvc newAddProductsToListViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new AddProductsToListViewMvc(layoutInflater, viewGroup);
    }

    public ChooseGroupViewMvc newChooseGroupViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new ChooseGroupViewMvc(layoutInflater, viewGroup);
    }

    public ChooseProductsFromGroupViewMvc newChooseProductsFromGroupViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new ChooseProductsFromGroupViewMvc(layoutInflater, viewGroup);
    }

    public EditProductViewMvc newEditProductViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new EditProductViewMvc(layoutInflater, viewGroup);
    }

    public GroupActivityViewMvc newGroupActivityViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new GroupActivityViewMvc(layoutInflater, viewGroup);
    }

    public ListActivityViewMvc newListActivityViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new ListActivityViewMvc(layoutInflater, viewGroup);
    }

    public ShareActivityViewMvc newShareActivityViewMvcInstance(@Nullable ViewGroup viewGroup)
    {
        return new ShareActivityViewMvc(layoutInflater, viewGroup);
    }

}
