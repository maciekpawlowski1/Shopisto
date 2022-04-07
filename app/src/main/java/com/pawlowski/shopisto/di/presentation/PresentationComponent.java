package com.pawlowski.shopisto.di.presentation;

import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.account.register_activity.RegisterActivity;
import com.pawlowski.shopisto.add_friend_activity.AddFriendActivity;
import com.pawlowski.shopisto.add_group_activity.AddGroupActivity;
import com.pawlowski.shopisto.add_products_to_list_activity.AddProductsToListActivity;
import com.pawlowski.shopisto.choose_group_activity.ChooseGroupActivity;
import com.pawlowski.shopisto.choose_products_from_group_activity.ChooseProductsFromGroupActivity;
import com.pawlowski.shopisto.di.ViewMvcFactory;
import com.pawlowski.shopisto.edit_product_activity.EditProductActivity;
import com.pawlowski.shopisto.group_activity.GroupActivity;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.share_activity.ShareActivity;

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
    void inject(ChooseGroupActivity activity);
    void inject(ListActivity activity);
    void inject(EditProductActivity activity);
    void inject(ShareActivity activity);
    void inject(ChooseProductsFromGroupActivity activity);
    void inject(GroupActivity activity);
}
