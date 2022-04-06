package com.pawlowski.shopisto.di.presentation;

import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.account.register_activity.RegisterActivity;
import com.pawlowski.shopisto.add_friend_activity.AddFriendActivity;
import com.pawlowski.shopisto.add_group_activity.AddGroupActivity;
import com.pawlowski.shopisto.add_products_to_list_activity.AddProductsToListActivity;
import com.pawlowski.shopisto.di.ViewMvcFactory;

import dagger.Subcomponent;

@PresentationScope
@Subcomponent(modules = {PresentationModule.class})
public interface PresentationComponent {

    ViewMvcFactory viewMvcFactory();
    void inject(LoginActivity activity);
    void inject(AddGroupActivity activity);
    void inject(AddFriendActivity activity);
    void inject(RegisterActivity activity);
    void inject(AddProductsToListActivity activity);
}
