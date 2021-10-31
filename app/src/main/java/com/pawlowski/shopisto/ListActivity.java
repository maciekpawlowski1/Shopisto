package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.account.LoginActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ListActivity extends BaseActivity {

    FloatingActionButton addButton;
    FloatingActionButton addGroupButton;
    FloatingActionButton addProductsButton;
    LinearLayout linearButtonLayout;
    ListAdapter adapter;
    RecyclerView recyclerView;
    int listId;
    boolean amIOwner;
    TextView textEmptyList;
    String listTittle;
    ImageView imageEmptyList;
    //ArrayList<ProductModel> products = new ArrayList<>();
    MenuItem editProductItem;
    MenuItem deleteProductItem;
    MenuItem shareProductItem;
    MenuItem addFromGroupItem;
    String listKey;
    boolean justCreated;
    List<FriendModel>friendsFromList = new ArrayList<>();
    boolean buttonsVisibility = false;

    ValueEventListener productsListener;
    ValueEventListener friendsListener;

    SwipeRefreshLayout swipeRefreshLayout;

    final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 60;

    AdView mAdView;

    CountDownTimer downloadTimer;
    CountDownTimer stoppingTimer;
    boolean changingActivity = false;

    boolean isOfflineMode = false;
    //boolean isCheckingForUpdates = false;






    //Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Log.d("onCreate", "onCreate method starts");

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle", " ");
        listKey = bundle.getString("listKey");
        justCreated = bundle.getBoolean("justCreated", false);
        amIOwner = bundle.getBoolean("amIOwner");

        //Log.d("onCreate listKey", listKey);
        /*toolbar = findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle(listTittle);
        //getSupportActionBar().setHomeButtonEnabled(true);
        addButton = findViewById(R.id.add_button_list);
        addGroupButton = findViewById(R.id.add_group_button_list);
        addProductsButton = findViewById(R.id.add_products_button_list);
        linearButtonLayout = findViewById(R.id.linear_layout_buttons_list);


        swipeRefreshLayout = findViewById(R.id.refresh_layout_list_activity);


        recyclerView = findViewById(R.id.recycler_list);

        isOfflineMode = isOfflineModeOn();


        addButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.isSomethingSelected())
                    adapter.unselectAllProducts();

                if(buttonsVisibility == false)
                {
                    showButtons();
                }
                else
                {
                    hideButtons();
                }

                return true;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.isSomethingSelected())
                    adapter.unselectAllProducts();


                if(!buttonsVisibility)
                {
                    Intent i = new Intent(ListActivity.this, AddProductsToListActivity.class);
                    i.putExtra("listId", listId);
                    i.putExtra("listKey", listKey);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
                else
                {
                    hideButtons();
                }


            }
        });



        addProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.isSomethingSelected())
                    adapter.unselectAllProducts();

                Intent i = new Intent(ListActivity.this, AddProductsToListActivity.class);
                i.putExtra("listId", listId);
                i.putExtra("listKey", listKey);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            }
        });

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(adapter.isSomethingSelected())
                    adapter.unselectAllProducts();

                Intent i = new Intent(ListActivity.this, ChooseGroupActivity.class);
                i.putExtra("listId", listId);
                i.putExtra("listKey", listKey);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);


            }
        });




        adapter = new ListAdapter(this, listId, listKey, isOfflineMode);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        textEmptyList = findViewById(R.id.text_empty_list);
        imageEmptyList = findViewById(R.id.image_view_empty_list);

        textEmptyList.setVisibility(View.GONE);
        imageEmptyList.setVisibility(View.GONE);


        imageEmptyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListActivity.this, AddProductsToListActivity.class);
                i.putExtra("listId", listId);
                i.putExtra("listKey", listKey);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isOfflineModeOn())//!amIOwner || (friendsFromList.size() != 0))
                {
                    FirebaseDatabase.getInstance().goOnline();
                    resetTimersAndStartStop();
                    if(!canIDownload())
                    {
                        Log.d("CanIDownload", "You have to wait");


                        new Handler().postDelayed(new Runnable() {
                            @Override public void run() {
                                if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                                    swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 500);

                    }
                    else
                    {

                        Log.d("CanIDownload", "You Can and downloaded");
                        saveDownloadTime();

                        checkingForUpdatesInASectionAction();

                        if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                            swipeRefreshLayout.setRefreshing(false);
                    }
                }
                else
                {
                    if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                }



            }
        });







    }

    @Override
    protected void onStart() {
        super.onStart();


        downloadTimer = new CountDownTimer(120000, 120000) {
            @Override
            public void onTick(long millisUntilFinished) {


            }

            @Override
            public void onFinish() {
                FirebaseDatabase.getInstance().goOnline();
                Log.d("ConnectionByTimer", "goOnline");
                if(stoppingTimer != null) {
                    stoppingTimer.cancel();
                    stoppingTimer.start();
                }

            }
        };

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
                    if(downloadTimer != null)
                    {
                        downloadTimer.cancel();
                        downloadTimer.start();
                    }
                }

            }
        };


        if(!isOfflineModeOn())
            FirebaseDatabase.getInstance().goOnline();

        loadProductsInAsyncTask();
        loadFriends();

        mAdView = findViewById(R.id.ad_view_list_activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if(!isOfflineModeOn())
        {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            productsListener = FirebaseDatabase.getInstance().getReference().child("d")
                    .child(user.getUid())
                    .child(listKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                String timestamp = snapshot.getValue().toString();
                                //Log.d("checking", timestamp+"");
                                resetTimersAndStartStop();

                                if(!DBHandler.getInstance(getApplicationContext()).isListSynced(listKey, timestamp))
                                {
                                    Log.d("syncing", "Not synced");
                                    saveDownloadTime();


                                    downloadProducts(timestamp);
                                /*.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists())
                                        {
                                            Log.d("downloading", "downloading started");
                                            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) snapshot.getValue();
                                            Object[] mapKeys = map.keySet().toArray();
                                            List<ProductModel> newProducts = new ArrayList<>();
                                            for(Object o:mapKeys)
                                            {
                                                //Log.d("downloading", o.toString());
                                                Map<String, Object>productMap = (Map<String, Object>) map.get(o.toString());
                                                int number = Integer.parseInt(productMap.get("n").toString());
                                                boolean selected = Boolean.parseBoolean(productMap.get("s").toString());
                                                int category = Integer.parseInt(productMap.get("c").toString());
                                                String description = "";
                                                if(productMap.containsKey("d"))
                                                    description = productMap.get("d").toString();


                                                ProductModel product = new ProductModel(o.toString(), description, selected, number);
                                                product.setCategoryId(category);
                                                newProducts.add(product);
                                                Log.d("downloading", product.toString());
                                            }

                                            DBHandler.getInstance(getApplicationContext()).syncList(newProducts, listId);
                                            loadProductsInAsyncTask();
                                        }

                                        DBHandler.getInstance(getApplicationContext()).updateListDownloadTimestamp(listKey, timestamp);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("Download", "Failed to download");
                                    }
                                });*/

                                }
                                else
                                {
                                    Log.d("syncing", "Synced");
                                }
                            }
                            else
                            {
                                //Log.d("ListActivity", "timestamp doesn't exist");
                                if(!justCreated)
                                {
                                    Toast.makeText(ListActivity.this, getString(R.string.you_were_probably_deleted_from_list), Toast.LENGTH_LONG).show();
                                    onBackPressed(); //Probably somebody removed us from list
                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


            friendsListener = FirebaseDatabase.getInstance().getReference().child("f")
                    .child(user.getUid())
                    .child(listKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                String timestamp = snapshot.getValue().toString();
                                if(!DBHandler.getInstance(getApplicationContext()).areListFriendsSynced(listKey, timestamp))
                                {
                                    Log.d("syncing", "friends not synced");

                                    FirebaseDatabase.getInstance().getReference().child("lu").child(listKey)
                                            .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                        @Override
                                        public void onSuccess(DataSnapshot dataSnapshot) {
                                            String owner = "";
                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                            //Log.d("map", map.toString());
                                            Object[] friends_uids = map.keySet().toArray();
                                            List<FriendModel>mails = new ArrayList<>();
                                            for(Object o:friends_uids)
                                            {
                                                String uid = o.toString();
                                                if(uid.equals("o"))
                                                {
                                                    owner = map.get(uid).toString();
                                                }
                                                else if(!uid.equals(user.getUid()) && !uid.equals("a"))
                                                {
                                                    String mail = map.get(uid).toString();
                                                    FriendModel fr = new FriendModel("", mail, true, false);
                                                    fr.setUid(uid);
                                                    //Log.d("NewMails", fr.getMail());
                                                    mails.add(fr);

                                                    //Log.d("friends", mail + " " + mail.length());
                                                }
                                            }

                                            //if(!ListActivity.this.isDestroyed())
                                            //showProgressDialog(getString(R.string.please_wait));
                                            DBHandler.getInstance(getApplicationContext()).syncListFriends(mails, listId, owner, timestamp, listKey);
                                            loadFriends();
                                            //if(!ListActivity.this.isDestroyed())
                                            //hideProgressDialog();
                                        }
                                    });


                                }
                                else
                                {
                                    Log.d("syncing", "friends synced");
                                }
                            }
                            else
                            {
                    /*Log.d("f", "onBackPressed action f");
                    if(!justCreated)
                    {
                        onBackPressed();
                    }*/
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }



        /*ArrayList<ProductModel> products = new ArrayList<>(DBHandler.getInstance(getApplicationContext()).getAllProductOfList(listId));
        if(products.size() > 0)
        {
            textEmptyList.setVisibility(View.GONE);
            imageEmptyList.setVisibility(View.GONE);
        }
        else
        {
            textEmptyList.setVisibility(View.VISIBLE);
            imageEmptyList.setVisibility(View.VISIBLE);
        }
        adapter.setProducts(products);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        downloadTimer.cancel();
        stoppingTimer.cancel();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(productsListener != null)
        {

            FirebaseDatabase.getInstance().getReference().child("d").child(user.getUid())
                    .child(listKey).removeEventListener(productsListener);
            productsListener = null;
        }

        if(friendsListener != null)
        {
            FirebaseDatabase.getInstance().getReference().child("f").child(user.getUid())
                    .child(listKey).removeEventListener(friendsListener);
            friendsListener = null;

        }





        if(!changingActivity)
            FirebaseDatabase.getInstance().goOffline();

    }


    public void checkingForUpdatesInASectionAction()
    {
        resetTimersAndStartStop();
        FirebaseDatabase.getInstance().goOnline();
        Log.d("checkingForUpdates", "Action A");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().goOnline();
        FirebaseDatabase.getInstance().getReference().child("l")
                .child(listKey)
                .child("a").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String timestamp = dataSnapshot.getValue().toString();
                    //Log.d("checking", timestamp+"");
                    //Log.d("timestamp A", timestamp);
                    if(!DBHandler.getInstance(getApplicationContext()).isListSynced(listKey, timestamp))
                    {
                        Log.d("syncingA", "Not synced");
                        saveDownloadTime();


                        downloadProducts();
                                /*.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists())
                                        {
                                            Log.d("downloading", "downloading started");
                                            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) snapshot.getValue();
                                            Object[] mapKeys = map.keySet().toArray();
                                            List<ProductModel> newProducts = new ArrayList<>();
                                            for(Object o:mapKeys)
                                            {
                                                //Log.d("downloading", o.toString());
                                                Map<String, Object>productMap = (Map<String, Object>) map.get(o.toString());
                                                int number = Integer.parseInt(productMap.get("n").toString());
                                                boolean selected = Boolean.parseBoolean(productMap.get("s").toString());
                                                int category = Integer.parseInt(productMap.get("c").toString());
                                                String description = "";
                                                if(productMap.containsKey("d"))
                                                    description = productMap.get("d").toString();


                                                ProductModel product = new ProductModel(o.toString(), description, selected, number);
                                                product.setCategoryId(category);
                                                newProducts.add(product);
                                                Log.d("downloading", product.toString());
                                            }

                                            DBHandler.getInstance(getApplicationContext()).syncList(newProducts, listId);
                                            loadProductsInAsyncTask();
                                        }

                                        DBHandler.getInstance(getApplicationContext()).updateListDownloadTimestamp(listKey, timestamp);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("Download", "Failed to download");
                                    }
                                });*/

                    }
                    else
                    {
                        Log.d("syncingA", "Synced");
                        FirebaseDatabase.getInstance().goOffline();
                        stoppingTimer.cancel();
                        downloadTimer.cancel();
                        downloadTimer.start();
                        Log.d("Connection_list_a", "goOffline");
                    }
                }
            }
        });




    }


    public boolean canIDownload()
    {
        //Log.d("canICownload", "Checking");
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastDownload = sharedPreferences.getLong("lastDownload" + listId, 0);
        if(date.getTime() - lastDownload > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //60000 milliseconds - 60 seconds
            return true;
        else if (lastDownload - date.getTime() > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //if somebody changes device time
            return true;
        else
            return false;

    }

    public void saveDownloadTime()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastDownload" + listId, date.getTime());
        editor.commit();
    }


    private void downloadProducts(String timestamp)
    {
        FirebaseDatabase.getInstance().getReference().child("p").child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                DataSnapshot snapshot = dataSnapshot;

                //FirebaseDatabase.getInstance().goOffline();
                //Log.d("Connection_list_a", "goOffline");
                resetTimersAndStartStop();

                if(snapshot.exists())
                {

                    //Log.d("problem1", snapshot.getValue().toString());
                    //Log.d("downloading", "downloading started");
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) snapshot.getValue();
                    Object[] mapKeys = map.keySet().toArray();
                    List<ProductModel> newProducts = new ArrayList<>();
                    for(Object o:mapKeys)
                    {
                        //Log.d("downloading", o.toString());
                        Map<String, Object>productMap = (Map<String, Object>) map.get(o.toString());
                        int number;
                        if(productMap.containsKey("n"))
                        {
                            number = Integer.parseInt(productMap.get("n").toString());
                        }
                        else
                            number = 1;


                        boolean selected = Boolean.parseBoolean(productMap.get("s").toString());

                        int category;
                        if(productMap.containsKey("c"))
                        {
                            category = Integer.parseInt(productMap.get("c").toString());
                        }
                        else
                            category = 0;

                        String description = "";
                        if(productMap.containsKey("d"))
                            description = productMap.get("d").toString();


                        ProductModel product = new ProductModel(o.toString(), description, selected, number);
                        product.setCategoryId(category);
                        newProducts.add(product);
                        //Log.d("downloading", product.toString());
                    }

                    DBHandler.getInstance(getApplicationContext()).syncList(newProducts, listId);
                    loadProductsInAsyncTask();
                }

                if(timestamp.length() != 0)
                    DBHandler.getInstance(getApplicationContext()).updateListDownloadTimestamp(listKey, timestamp);


            }
        });
    }

    private void downloadProducts()
    {
        downloadProducts("");
    }

    public void loadProductsInAsyncTask()
    {
        //Loading lists outside UI Thread
        new AsyncTask<Void, Integer, Void>() {

            WeakReference<ListAdapter> adapter;
            WeakReference<TextView> textEmpty;
            WeakReference<ImageView> imageEmpty;
            WeakReference<ListActivity>activity;
            ArrayList<ProductModel>products;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                this.adapter = new WeakReference<>(ListActivity.this.adapter);
                this.activity = new WeakReference<>(ListActivity.this);
                this.textEmpty = new WeakReference<>(textEmptyList);
                this.imageEmpty = new WeakReference<>(imageEmptyList);
                //if(!activity.get().isDestroyed())
                    //activity.get().showProgressDialog(getString(R.string.please_wait));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                products = new ArrayList<>(DBHandler.getInstance(activity.get().getApplicationContext()).getAllProductOfList(listId));
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                //activity.get().hideProgressDialog();
                if(products.size() > 0)
                {
                    textEmpty.get().setVisibility(View.GONE);
                    imageEmpty.get().setVisibility(View.GONE);
                }
                else
                {
                    textEmpty.get().setVisibility(View.VISIBLE);
                    imageEmpty.get().setVisibility(View.VISIBLE);
                }
                adapter.get().setProducts(products);
            }
        }.execute();
    }

    public void loadFriends()
    {
        friendsFromList = DBHandler.getInstance(getApplicationContext()).getFriendsWithoutNicknamesFromThisList(listId);

    }

    public void showNoProductsImage()
    {
        textEmptyList.setVisibility(View.VISIBLE);
        imageEmptyList.setVisibility(View.VISIBLE);
    }

    public void hideNoProductsImage()
    {
        textEmptyList.setVisibility(View.GONE);
        imageEmptyList.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.products_in_list_menu, menu);
        editProductItem = menu.findItem(R.id.edit_products_in_list_menu);
        deleteProductItem = menu.findItem(R.id.delete_products_in_list_menu);
        shareProductItem = menu.findItem(R.id.share_products_in_list_menu);
        addFromGroupItem = menu.findItem(R.id.add_group_products_in_list_menu);
        deleteProductItem.setVisible(false);
        editProductItem.setVisible(false);
        shareProductItem.setVisible(true);
        addFromGroupItem.setVisible(true);
        editProductItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Edit product activity open
                editAction(true, null);

                return true;
            }
        });
        deleteProductItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Delete product action
                resetTimersAndStartStop();
                FirebaseDatabase.getInstance().goOnline();
                adapter.deleteSelectedProducts(recyclerView);
                //adapter.unselectAllProducts();
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
        else if(item.getItemId() == R.id.share_products_in_list_menu)
        {
            if(adapter.isSomethingSelected())
                adapter.unselectAllProducts();

            if(!isOfflineModeOn())
            {
                Intent i = new Intent(ListActivity.this, ShareActivity.class);

                i.putExtra("listId", listId);
                i.putExtra("listTittle", listTittle);
                i.putExtra("listKey", listKey);
                changingActivity = true;
                startActivity(i);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.login_before_sharing), Toast.LENGTH_LONG).show();
                Intent i = new Intent(ListActivity.this, LoginActivity.class);
                startActivity(i);

            }
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);


        }
        else if(item.getItemId() == R.id.add_group_products_in_list_menu)
        {
            if(adapter.isSomethingSelected())
                adapter.unselectAllProducts();

            Intent i = new Intent(ListActivity.this, ChooseGroupActivity.class);
            i.putExtra("listId", listId);
            i.putExtra("listKey", listKey);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        }

        return super.onOptionsItemSelected(item);
    }

    public void setMenuVisible()
    {
        editProductItem.setVisible(true);
        deleteProductItem.setVisible(true);
        shareProductItem.setVisible(false);
        addFromGroupItem.setVisible(false);
    }

    public void setMenuInvisible()
    {
        editProductItem.setVisible(false);
        deleteProductItem.setVisible(false);
        shareProductItem.setVisible(true);
        addFromGroupItem.setVisible(true);
    }

    public void showOnlyDeleteItem()
    {
        editProductItem.setVisible(false);
        deleteProductItem.setVisible(true);
        shareProductItem.setVisible(false);
        addFromGroupItem.setVisible(false);
    }


    @Override
    public void onBackPressed() {
        if(adapter.isSomethingSelected())
        {
            adapter.unselectAllProducts();
        }
        else
        {
            //Log.d("onBackPressed", "onBackPressed action");
            changingActivity = true;
            super.onBackPressed();
        }
    }

    public void showButtons()
    {
        linearButtonLayout.setVisibility(View.VISIBLE);
        linearButtonLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.buttons_show_animation));
        buttonsVisibility = true;
        addButton.setImageResource(R.drawable.cancel_icon2);
    }

    public void hideButtons()
    {
        linearButtonLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.buttons_hide_animation));
        linearButtonLayout.setVisibility(View.GONE);
        buttonsVisibility = false;
        addButton.setImageResource(R.drawable.plus_icon);
    }

    public void editAction(boolean fromActionBar, ProductModel selectedProductIfNotFromActionBar)
    {
        Intent i = new Intent(ListActivity.this, EditProductActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        ProductModel product;
        if(fromActionBar)
        {
            product = adapter.getSelectedProducts().get(0);
        }
        else
        {
            product = selectedProductIfNotFromActionBar;
        }

        adapter.unselectAllProducts();

                /*i.putExtra("productTittle", product.getTittle());
                i.putExtra("productDescription", product.getDescription());
                i.putExtra("productId", product.getId());
                i.putExtra("productNumber", product.getNumber());*/
        i.putExtra("product", product);
        i.putExtra("productSelected", product.isSelected());
        i.putExtra("category_id", product.getCategoryId());
        //changingActivity = true;


        startActivity(i);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        finish();
    }

    public List<FriendModel>getFriendsFromList()
    {
        return friendsFromList;
    }

    public void resetTimersAndStartStop()
    {
        downloadTimer.cancel();
        stoppingTimer.cancel();
        stoppingTimer.start();
    }


}