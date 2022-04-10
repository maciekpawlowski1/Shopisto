package com.pawlowski.shopisto.group_activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.edit_product_activity.EditProductActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.models.ProductModel;
import com.pawlowski.shopisto.share_activity.ShareActivity;

import javax.inject.Inject;

public class GroupActivity extends BaseActivity implements GroupActivityViewMvc.GroupActivityButtonsClickListener {

    private int groupId;
    private String groupKey;

    private ProductsInGroupAdapter adapter;

    private MenuItem editProductItem;
    private MenuItem deleteProductItem;

    private CountDownTimer stoppingTimer;

    private boolean changingActivity = false;

    private GroupActivityViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int groupId, String groupTittle, String groupKey)
    {
        Intent i = new Intent(context, GroupActivity.class);
        i.putExtra("groupId", groupId);
        i.putExtra("groupTittle", groupTittle);
        i.putExtra("groupKey", groupKey);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newGroupActivityViewMvcInstance(null);
        setContentView(viewMvc.getRootView());

        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getInt("groupId", -1);
        String groupTittle = bundle.getString("groupTittle", " ");
        groupKey = bundle.getString("groupKey");

        getSupportActionBar().setTitle(groupTittle);
        adapter = new ProductsInGroupAdapter(this, groupId, false, groupKey, dbHandler);
        viewMvc.setRecyclerAdapter(adapter);


        hideNoProductsImage();

        AdRequest adRequest = new AdRequest.Builder().build();
        viewMvc.loadAd(adRequest);

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        boolean offlineMode = isOfflineModeOn();
        if(!offlineMode)
            FirebaseDatabase.getInstance().goOnline();


        adapter.setProducts(dbHandler.getAllProductsFromGroup(groupId));

        if(adapter.getItemCount() == 0)
        {
            showNoProductsImage();
        }
        else
        {
            hideNoProductsImage();
        }


        stoppingTimer = new CountDownTimer(10000, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(!changingActivity)
                {
                    FirebaseDatabase.getInstance().goOffline();
                    Log.d("ConnectionByTimer", "goOffline");
                }
            }
        };
        if(offlineMode)
            stoppingTimer.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    public void showNoProductsImage()
    {
        viewMvc.showNoProductsImage();
    }

    public void hideNoProductsImage()
    {
        viewMvc.hideNoProductsImage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products_in_list_menu, menu);

        editProductItem = menu.findItem(R.id.edit_products_in_list_menu);
        deleteProductItem = menu.findItem(R.id.delete_products_in_list_menu);
        deleteProductItem.setVisible(false);
        editProductItem.setVisible(false);

        menu.findItem(R.id.share_products_in_list_menu).setVisible(false);
        menu.findItem(R.id.add_group_products_in_list_menu).setVisible(false);


        editProductItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Edit product activity open

                ProductModel product = adapter.getSelectedProduct();
                adapter.unselectProduct();

                EditProductActivity.launchFromGroups(GroupActivity.this, true, groupId, groupKey, product.getCategoryId(), product);

                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                return true;
            }
        });
        deleteProductItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Delete product action
                resetTimer();
                FirebaseDatabase.getInstance().goOnline();
                adapter.deleteSelectedProducts(viewMvc.getRootView());
                //adapter.unselectProduct();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void resetTimer()
    {
        stoppingTimer.cancel();
        stoppingTimer.start();
    }


    public void setMenuVisible()
    {
        editProductItem.setVisible(true);
        deleteProductItem.setVisible(true);
    }

    public void setMenuInvisible()
    {
        editProductItem.setVisible(false);
        deleteProductItem.setVisible(false);
    }

    public void showOnlyDeleteMenuItem()
    {
        editProductItem.setVisible(false);
        deleteProductItem.setVisible(true);
    }


    @Override
    public void onBackPressed() {
        if(!adapter.isSomethingSelected())
        {
            super.onBackPressed();
        }
        else
        {
            changingActivity = true;
            adapter.unselectAllProducts();
        }

    }

    @Override
    public void onAddButtonClick() {
        EditProductActivity.launchFromGroups(this, true, groupId, groupKey, 0, new ProductModel("", "", false, 1));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}