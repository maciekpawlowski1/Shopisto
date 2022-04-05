package com.pawlowski.shopisto.main.products_fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.add_group_activity.AddGroupActivity;
import com.pawlowski.shopisto.MyFragmentHolder;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.main.MainActivity;
import com.pawlowski.shopisto.main.ProductGroupsAdapter;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;


public class ProductsFragment extends MyFragmentHolder implements ProductsFragmentViewMvc.ProductsFragmentButtonsClickListener {

    private static final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 300;
    ProductGroupsAdapter adapter;

    boolean changingActivity = false;
    //SwipeRefreshLayout swipeRefreshLayout;

    List<Boolean> downloading = new ArrayList<>();

    CountDownTimer stopTimer;

    MainActivity activity;

    private ProductsFragmentViewMvc viewMvc;

    public ProductsFragment() {
        // Required empty public constructor
    }

    public ProductsFragment(MainActivity activity) {
        this.activity = activity;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewMvc = new ProductsFragmentViewMvc(inflater, container);

        adapter = new ProductGroupsAdapter(activity, false, -1, this);
        viewMvc.setRecyclerAdapter(adapter);
        viewMvc.hideNoGroupsItems();

        activity.getSupportActionBar().setTitle(R.string.templates);

        return viewMvc.getRootView();
    }


    public void navigateToAddingGroupsAction()
    {
        AddGroupActivity.launch(getContext());
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }

    public void showNoGroupsItems()
    {
        viewMvc.showNoGroupsItems();
    }

    public void hideNoGroupsItems()
    {
        viewMvc.hideNoGroupsItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        stopTimer = new CountDownTimer(10000, 10000) {
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

        loadGroups();

        if(canIDownload() && !activity.isOfflineModeOn()) {
            FirebaseDatabase.getInstance().goOnline();
            checkingForUpdatesAction();
            Log.d("downloadingGroups", "Can and downloaded");
        }
        else
        {
            Log.d("downloadingGroups", "Waiting");
            FirebaseDatabase.getInstance().goOffline();
        }

    }


    public void resetTimer()
    {
        stopTimer.cancel();
        stopTimer.start();
    }

    public void loadGroups()
    {
        adapter.setGroups(DBHandler.getInstance(getActivity().getApplicationContext()).getAllGroups());
        if(adapter.getItemCount() == 0)
            viewMvc.showNoGroupsItems();
        else
            viewMvc.hideNoGroupsItems();
    }

    public void scrollToTheStarting()
    {
        viewMvc.scrollToRecyclerTop();
    }


    public boolean canIDownload()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastDownload = sharedPreferences.getLong("lastGroupsDownload", 0);
        if(date.getTime() - lastDownload > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //60000 milliseconds - 60 seconds
            return true;
        else if (lastDownload - date.getTime() > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //if somebody changes device time
            return true;
        else
            return wasRecentlyLogOut(getActivity(), SECONDS_TO_NEXT_SELF_DOWNLOAD);

    }


    public static boolean wasRecentlyLogOut(Activity activity, int seconds)
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastDownload = sharedPreferences.getLong("lastLogOut", 0);
        if(date.getTime() - lastDownload > (seconds * 1000L)) //60000 milliseconds - 60 seconds
            return false;
        else
            return true;

    }

    public void saveDownloadTime()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastGroupsDownload", date.getTime());
        editor.commit();
    }


    public void checkingForUpdatesAction()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String userEmail = user.getEmail();
        DBHandler dbHandler = DBHandler.getInstance(getActivity().getApplicationContext());



        FirebaseDatabase.getInstance().getReference().child("t").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    saveDownloadTime();
                    Set<String> lastKeys = dbHandler.getGroupsKeysSet();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Set<String> newKeys = map.keySet();

                    //boolean somethingToDelete = false;
                    for(String lastKey:lastKeys)
                    {
                        if(!newKeys.contains(lastKey))
                        {
                            //Delete list
                            int id = dbHandler.getGroupIdByKey(lastKey);
                            dbHandler.deleteGroup(id);
                            //somethingToDelete = true;

                        }
                    }
                    boolean somethingToDownload = false;
                    for(String key:newKeys)
                    {
                        if(!lastKeys.contains(key))
                        {
                            downloading.add(true);
                        }
                        else
                        {
                            long newTimestamp = (long) map.get(key);
                            long last = dbHandler.getGroupDownloadTimestamp(key);
                            if(last != newTimestamp)
                            {
                                downloading.add(true);
                            }
                        }
                    }

                    for(String key:newKeys)
                    {
                        if(lastKeys.contains(key))
                        {
                            //Can check for update or not

                            long newTimestamp = (long) map.get(key);
                            long last = dbHandler.getGroupDownloadTimestamp(key);
                            if(last != newTimestamp)
                            {
                                downloading.add(true);
                                downloadAndUpdate(key, userEmail, dbHandler, (Long) map.get(key));
                            }
                        }
                        else
                        {
                            somethingToDownload = true;
                            //if(downloading.size() == 0)
                            //activity.showProgressDialog(getString(R.string.please_wait));
                            //downloading.add(true);
                            //downloadList(key, map.get(key).toString(), userEmail, dbHandler);
                            downloadGroup(key, userEmail, dbHandler, (Long) map.get(key));
                        }
                    }

                    if(!somethingToDownload)
                    {
                        loadGroups();
                        //if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                            //swipeRefreshLayout.setRefreshing(false);
                        if(!changingActivity)
                        {
                            FirebaseDatabase.getInstance().goOffline();
                            Log.d("connection", "goOffline");
                        }

                    }


                }
                else
                {
                    DBHandler.getInstance(getActivity().getApplicationContext()).deleteAllGroups();
                    loadGroups();
                    if(!changingActivity)
                    {
                        FirebaseDatabase.getInstance().goOffline();
                        Log.d("connection", "goOffline");
                    }
                }
            }
        });
    }


    public void downloadGroup(String listKey, String userEmail, DBHandler dbHandler, long downloadTimestamp)
    {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        reference.child("g").child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Map<String, Object>map = (Map<String, Object>) dataSnapshot.getValue();
                String createTimestamp = map.get("c").toString();
                String tittle = map.get("t").toString();
                GroupModel group = new GroupModel(tittle);
                group.setKey(listKey);
                //Log.d("map", map.toString());
                if(map.containsKey("p"))
                {
                    Map<String, Object>allProductsMap = (Map<String, Object>) map.get("p");
                    //Log.d("allMap", allProductsMap.toString());

                    ArrayList<ProductModel> newProducts = new ArrayList<>();
                    for(Object o:allProductsMap.keySet())
                    {
                        //Log.d("downloading", o.toString());
                        Map<String, Object>productMap = (Map<String, Object>) allProductsMap.get(o.toString());
                        int number = 1;
                        if(productMap.containsKey("n"))
                            number = Integer.parseInt(productMap.get("n").toString());
                        String description = "";
                        if(productMap.containsKey("d"))
                            description = productMap.get("d").toString();

                        //boolean selected = Boolean.parseBoolean(productMap.get("s").toString());
                        //if(selected)
                            //numberSelected++;
                        int category = 0;
                        if(productMap.containsKey("c"))
                            category = Integer.parseInt(productMap.get("c").toString());

                        ProductModel product = new ProductModel(o.toString(), description, false, number);
                        product.setCategoryId(category);
                        newProducts.add(product);
                        //Log.d("downloading", product.toString());
                    }

                    group.setProducts(newProducts);

                }
                else
                {
                    group.setProducts(new ArrayList<>());
                }
                group.setDownloadTimestamp(downloadTimestamp);

                dbHandler.insertGroup(group, createTimestamp);

                if(downloading.size() == 1)
                {

                    loadGroups();

                    //if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                        //swipeRefreshLayout.setRefreshing(false);

                    if(!changingActivity)
                    {
                        FirebaseDatabase.getInstance().goOffline();
                        Log.d("connection", "goOffline");
                    }

                }
                downloading.remove(0);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



    }


    public void downloadAndUpdate(String groupKey, String userEmail, DBHandler dbHandler, long downloadTimestamp)
    {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        reference.child("g").child(groupKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Map<String, Object>map = (Map<String, Object>) dataSnapshot.getValue();
                //String createTimestamp = map.get("c").toString();
                String tittle = map.get("t").toString();
                GroupModel group = new GroupModel(tittle);
                group.setKey(groupKey);
                //Log.d("map", map.toString());
                if(map.containsKey("p"))
                {
                    Map<String, Object>allProductsMap = (Map<String, Object>) map.get("p");
                    //Log.d("allMap", allProductsMap.toString());

                    ArrayList<ProductModel> newProducts = new ArrayList<>();
                    for(Object o:allProductsMap.keySet())
                    {
                        //Log.d("downloading", o.toString());
                        Map<String, Object>productMap = (Map<String, Object>) allProductsMap.get(o.toString());
                        int number = 1;
                        if(productMap.containsKey("n"))
                            number = Integer.parseInt(productMap.get("n").toString());
                        String description = "";
                        if(productMap.containsKey("d"))
                            description = productMap.get("d").toString();

                        //boolean selected = Boolean.parseBoolean(productMap.get("s").toString());
                        //if(selected)
                        //numberSelected++;
                        int category = 0;
                        if(productMap.containsKey("c"))
                            category = Integer.parseInt(productMap.get("c").toString());

                        ProductModel product = new ProductModel(o.toString(), description, false, number);
                        product.setCategoryId(category);
                        newProducts.add(product);
                        //Log.d("downloading", product.toString());
                    }

                    group.setProducts(newProducts);

                }
                else
                {
                    group.setProducts(new ArrayList<>());
                }
                group.setDownloadTimestamp(downloadTimestamp);

                dbHandler.syncGroup(group.getProducts(), dbHandler.getGroupIdByKey(groupKey), group.getTittle());

                if(downloading.size() == 1)
                {

                    loadGroups();

                    //if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                    //swipeRefreshLayout.setRefreshing(false);

                    if(!changingActivity)
                    {
                        FirebaseDatabase.getInstance().goOffline();
                        Log.d("connection", "goOffline");
                    }

                }
                downloading.remove(0);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



    }

    @Override
    public void onAddButtonClick() {
        navigateToAddingGroupsAction();
    }

    @Override
    public void onNoGroupsItemClick() {
        navigateToAddingGroupsAction();
    }
}