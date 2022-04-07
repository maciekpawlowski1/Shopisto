package com.pawlowski.shopisto.choose_products_from_group_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.group_activity.ProductsInGroupAdapter;
import com.pawlowski.shopisto.models.ProductModel;
import com.pawlowski.shopisto.share_activity.ShareActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class ChooseProductsFromGroupActivity extends BaseActivity {


    private int listId;
    private String listKey;
    private ProductsInGroupAdapter adapter;
    private ChooseProductsFromGroupViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int listId, int groupId, String groupKey, String listKey)
    {
        Intent i = new Intent(context, ChooseProductsFromGroupActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("groupId", groupId);
        i.putExtra("groupKey", groupKey);
        i.putExtra("listKey", listKey);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newChooseProductsFromGroupViewMvcInstance(null);
        setContentView(viewMvc.getRootView());
        getSupportActionBar().setTitle(getString(R.string.choose_products));

        Bundle bundle = getIntent().getExtras();
        int groupId = bundle.getInt("groupId", -1);
        listId = bundle.getInt("listId", -1);
        listKey = bundle.getString("listKey");
        String groupKey = bundle.getString("groupKey");


        adapter = new ProductsInGroupAdapter(this, groupId, true, groupKey);
        viewMvc.setRecyclerAdapter(adapter);

        adapter.setProducts(dbHandler.getAllProductsFromGroup(groupId));
    }

    private boolean addSelectedProductsToList()
    {
        List<ProductModel> selectedProducts = adapter.getSelectedProducts();
        if(selectedProducts.size() == 0)
            return false;
        else
        {
            ArrayList<ProductModel>listProducts = new ArrayList<>(dbHandler.getAllProductOfList(listId));
            ArrayList<ProductModel>productsToAddOnline = new ArrayList<>();
            for(ProductModel p:selectedProducts)
            {
                if(!isSuchTittleInProductList(p.getTittle(), listProducts))
                {
                    dbHandler.insertProduct(p, listId);
                    productsToAddOnline.add(p);
                }
            }

            if(productsToAddOnline.size() > 0 && !isOfflineModeOn())
            {
                OnlineDBHandler.addManyProductsWithDescription(listKey, productsToAddOnline,
                        dbHandler
                                .getFriendsWithoutNicknamesFromThisList(listId));

            }
            return true;
        }
    }

    boolean isSuchTittleInProductList(String tittle, ArrayList<ProductModel>products)
    {
        String smallTittle = tittle.toLowerCase();
        for(ProductModel p:products)
        {
            if(p.getTittle().toLowerCase().equals(smallTittle))
                return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        else if(item.getItemId() == R.id.finish_edit_menu)
        {

            if(addSelectedProductsToList())
            {

                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
            else
            {
                showErrorSnackbar(getResources().getString(R.string.first_choose_some_products), true);

            }

        }

        return super.onOptionsItemSelected(item);
    }
}