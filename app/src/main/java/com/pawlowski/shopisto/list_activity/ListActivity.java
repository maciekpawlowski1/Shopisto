package com.pawlowski.shopisto.list_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.add_products_to_list_activity.AddProductsToListActivity;
import com.pawlowski.shopisto.choose_group_activity.ChooseGroupActivity;
import com.pawlowski.shopisto.edit_product_activity.EditProductActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.share_activity.ShareActivity;
import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class ListActivity extends BaseActivity implements ListActivityViewMvc.ListActivityButtonsClickListener {


    private int listId;
    private ListAdapter adapter;
    private String listTittle;
    private MenuItem editProductItem;
    private MenuItem deleteProductItem;
    private MenuItem shareProductItem;
    private MenuItem addFromGroupItem;
    private String listKey;
    private boolean justCreated;
    private List<FriendModel>friendsFromList = new ArrayList<>();

    private ValueEventListener productsListener;
    private ValueEventListener friendsListener;

    private final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 60;

    private CountDownTimer downloadTimer;
    private CountDownTimer stoppingTimer;
    private boolean changingActivity = false;

    private ListActivityViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int listId, String listTittle, String listKey, boolean justCreated)
    {
        Intent i = new Intent(context, ListActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        i.putExtra("justCreated", justCreated);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newListActivityViewMvcInstance(null);
        setContentView(viewMvc.getRootView());

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle", " ");
        listKey = bundle.getString("listKey");
        justCreated = bundle.getBoolean("justCreated", false);

        getSupportActionBar().setTitle(listTittle);

        boolean isOfflineMode = isOfflineModeOn();

        adapter = new ListAdapter(this, listId, listKey, isOfflineMode, dbHandler);
        viewMvc.setRecyclerAdapter(adapter);

        viewMvc.hideEmptyListItems();

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);

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

        AdRequest adRequest = new AdRequest.Builder().build();
        viewMvc.loadAd(adRequest);

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

                                if(!dbHandler.isListSynced(listKey, timestamp))
                                {
                                    Log.d("syncing", "Not synced");
                                    saveDownloadTime();


                                    downloadProducts(timestamp);

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
                                if(!dbHandler.areListFriendsSynced(listKey, timestamp))
                                {
                                    Log.d("syncing", "friends not synced");

                                    FirebaseDatabase.getInstance().getReference().child("lu").child(listKey)
                                            .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                        @Override
                                        public void onSuccess(DataSnapshot dataSnapshot) {
                                            String owner = "";
                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
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
                                                    mails.add(fr);

                                                }
                                            }

                                            dbHandler.syncListFriends(mails, listId, owner, timestamp, listKey);
                                            loadFriends();
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

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
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
                    if(!dbHandler.isListSynced(listKey, timestamp))
                    {
                        Log.d("syncingA", "Not synced");
                        saveDownloadTime();


                        downloadProducts();

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
                resetTimersAndStartStop();

                if(snapshot.exists())
                {

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

                    dbHandler.syncList(newProducts, listId);
                    loadProductsInAsyncTask();
                }

                if(timestamp.length() != 0)
                    dbHandler.updateListDownloadTimestamp(listKey, timestamp);


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
            WeakReference<ListActivity>activity;
            WeakReference<ListActivityViewMvc>viewMvcWeak;
            ArrayList<ProductModel>products;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                this.adapter = new WeakReference<>(ListActivity.this.adapter);
                this.activity = new WeakReference<>(ListActivity.this);
                this.viewMvcWeak = new WeakReference<>(viewMvc);
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
                    viewMvcWeak.get().hideEmptyListItems();
                }
                else
                {
                    viewMvcWeak.get().showEmptyListItems();
                }
                adapter.get().setProducts(products);
            }
        }.execute();
    }

    public void loadFriends()
    {
        friendsFromList = dbHandler.getFriendsWithoutNicknamesFromThisList(listId);

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
                adapter.deleteSelectedProducts(viewMvc.getRootView());
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
                changingActivity = true;
                ShareActivity.launch(ListActivity.this, listId, listTittle, listKey);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.login_before_sharing), Toast.LENGTH_LONG).show();
                LoginActivity.launch(ListActivity.this);

            }
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);


        }
        else if(item.getItemId() == R.id.add_group_products_in_list_menu)
        {
            if(adapter.isSomethingSelected())
                adapter.unselectAllProducts();

            ChooseGroupActivity.launch(ListActivity.this, listId, listKey);
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



    public void editAction(boolean fromActionBar, ProductModel selectedProductIfNotFromActionBar)
    {
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

        EditProductActivity.launchFromLists(this, listId, listTittle, listKey, false, product.getCategoryId(), product);
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


    @Override
    public void onAddButtonClick() {
        if(adapter.isSomethingSelected())
            adapter.unselectAllProducts();


        if(!viewMvc.areButtonsVisible())
        {
            AddProductsToListActivity.launch(this, listId, listKey);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        else
        {
            viewMvc.hideButtons();
        }
    }

    @Override
    public void onAddButtonLongClick() {
        if(adapter.isSomethingSelected())
            adapter.unselectAllProducts();

        viewMvc.showOrHideButtons();
    }

    @Override
    public void onAddProductsButtonClick() {
        if(adapter.isSomethingSelected())
            adapter.unselectAllProducts();

        AddProductsToListActivity.launch(this, listId, listKey);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onAddGroupButtonClick() {
        if(adapter.isSomethingSelected())
            adapter.unselectAllProducts();

        ChooseGroupActivity.launch(this, listId, listKey);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onSwipeRefresh() {
        if(!isOfflineModeOn())//!amIOwner || (friendsFromList.size() != 0))
        {
            FirebaseDatabase.getInstance().goOnline();
            resetTimersAndStartStop();
            if(!canIDownload())
            {
                Log.d("CanIDownload", "You have to wait");

                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        viewMvc.stopRefreshing();
                    }
                }, 500);

            }
            else
            {

                Log.d("CanIDownload", "You Can and downloaded");
                saveDownloadTime();

                checkingForUpdatesInASectionAction();

                viewMvc.stopRefreshing();
            }
        }
        else
        {
            viewMvc.stopRefreshing();
        }
    }

    @Override
    public void onEmptyListImageClick() {
        AddProductsToListActivity.launch(this, listId, listKey);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}