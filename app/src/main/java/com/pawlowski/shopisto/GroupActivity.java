package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.models.ProductModel;

public class GroupActivity extends BaseActivity {

    int groupId;
    String groupTittle;
    String groupKey;
    RecyclerView recycler;
    ProductsInGroupAdapter adapter;

    FloatingActionButton addButton;

    MenuItem editProductItem;
    MenuItem deleteProductItem;

    ImageView noProductsImage;
    TextView noProductsText;

    AdView mAdView;

    CountDownTimer stoppingTimer;

    boolean changingActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getInt("groupId", -1);
        groupTittle = bundle.getString("groupTittle", " ");
        groupKey = bundle.getString("groupKey");
        /*toolbar = findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle(groupTittle);
        adapter = new ProductsInGroupAdapter(this, groupId, false, groupKey);


        recycler = findViewById(R.id.recycler_products_in_groups);
        addButton = findViewById(R.id.add_product_button_group);

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler.setAdapter(adapter);



        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupActivity.this, EditProductActivity.class);
                i.putExtra("group_id", groupId);
                i.putExtra("product", new ProductModel("", "", false, 1));
                i.putExtra("groups", true);
                i.putExtra("groupKey", groupKey);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });



        noProductsImage = findViewById(R.id.no_products_image_products_in_group);
        noProductsText = findViewById(R.id.no_products_text_products_in_groups);

        hideNoProductsImage();

        mAdView = findViewById(R.id.ad_view_group_activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean offlineMode = isOfflineModeOn();
        if(!offlineMode)
            FirebaseDatabase.getInstance().goOnline();


        adapter.setProducts(DBHandler.getInstance(getApplicationContext()).getAllProductsFromGroup(groupId));

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

    public void showNoProductsImage()
    {
        noProductsText.setVisibility(View.VISIBLE);
        noProductsImage.setVisibility(View.VISIBLE);
    }

    public void hideNoProductsImage()
    {
        noProductsText.setVisibility(View.GONE);
        noProductsImage.setVisibility(View.GONE);

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
                Intent i = new Intent(GroupActivity.this, EditProductActivity.class);
                i.putExtra("group_id", groupId);
                //i.putExtra("product", new ProductModel("", "", false, 1));
                i.putExtra("groups", true);
                i.putExtra("groupKey", groupKey);


                ProductModel product = adapter.getSelectedProduct();
                adapter.unselectProduct();
                /*i.putExtra("productTittle", product.getTittle());
                i.putExtra("productDescription", product.getDescription());
                i.putExtra("productId", product.getId());
                i.putExtra("productNumber", product.getNumber());*/
                i.putExtra("product", product);
                i.putExtra("category_id", product.getCategoryId());
                //i.putExtra("productSelected", product.isSelected());
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                //startActivity(i);
                //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                //finish();

                return true;
            }
        });
        deleteProductItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Delete product action
                resetTimer();
                FirebaseDatabase.getInstance().goOnline();
                adapter.deleteSelectedProducts(recycler);
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
        if(adapter.getHowManySelected() == 0)
        {
            super.onBackPressed();
        }
        else
        {
            changingActivity = true;
            adapter.unselectAllProducts();
        }

    }
}