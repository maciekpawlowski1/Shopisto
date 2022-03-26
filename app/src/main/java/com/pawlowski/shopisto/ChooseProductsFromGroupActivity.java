package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.group_activity.ProductsInGroupAdapter;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;

public class ChooseProductsFromGroupActivity extends BaseActivity {


    RecyclerView recyclerView;
    int groupId;
    int listId;
    String listKey;
    String groupKey;
    ProductsInGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_products_from_group);
        getSupportActionBar().setTitle(getString(R.string.choose_products));

        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getInt("groupId", -1);
        listId = bundle.getInt("listId", -1);
        listKey = bundle.getString("listKey");
        groupKey = bundle.getString("groupKey");


        recyclerView = findViewById(R.id.recycler_choose_products_in_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductsInGroupAdapter(this, groupId, true, groupKey);
        recyclerView.setAdapter(adapter);

        adapter.setProducts(DBHandler.getInstance(getApplicationContext()).getAllProductsFromGroup(groupId));
    }

    private boolean addSelectedProductsToList()
    {



        ArrayList<ProductModel> selectedProducts = adapter.getSelectedProducts();
        if(selectedProducts.size() == 0)
            return false;
        else
        {
            ArrayList<ProductModel>listProducts = new ArrayList<>(DBHandler.getInstance(getApplicationContext()).getAllProductOfList(listId));
            ArrayList<ProductModel>productsToAddOnline = new ArrayList<>();
            for(ProductModel p:selectedProducts)
            {
                if(!isSuchTittleInProductList(p.getTittle(), listProducts))
                {
                    DBHandler.getInstance(getApplicationContext()).insertProduct(p, listId);
                    productsToAddOnline.add(p);
                }
            }

            if(productsToAddOnline.size() > 0 && !isOfflineModeOn())
            {
                OnlineDBHandler.addManyProductsWithDescription(listKey, productsToAddOnline,
                        DBHandler.getInstance(getApplicationContext())
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