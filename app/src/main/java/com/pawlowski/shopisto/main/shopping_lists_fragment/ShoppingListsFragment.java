package com.pawlowski.shopisto.main.shopping_lists_fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.MyFragmentHolder;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.list_creating_activity.ListCreatingActivity;
import com.pawlowski.shopisto.main.MainActivity;
import com.pawlowski.shopisto.main.products_fragment.ProductsFragment;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;


public class ShoppingListsFragment extends MyFragmentHolder implements ShoppingListsFragmentViewMvc.ShoppingListsFragmentButtonsClickListener {

    private static final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 10;
    private ShoppingListsAdapter adapter;
    private final List<Boolean>downloading = new ArrayList<>();
    private MainActivity activity;
    private final DBHandler dbHandler;
    private CountDownTimer stopTimer;

    private boolean changingActivity = false;

    private ShoppingListsFragmentViewMvc viewMvc;


    public ShoppingListsFragment() {
        // Required empty public constructor
        throw new RuntimeException("Wrong constructor");
    }

    public ShoppingListsFragment(MainActivity activity, DBHandler dbHandler) {
        this.activity = activity;
        // Required empty public constructor
        this.dbHandler = dbHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewMvc = new ShoppingListsFragmentViewMvc(inflater, container);

        activity.getSupportActionBar().setTitle(R.string.lists);


        adapter = new ShoppingListsAdapter(activity, this, dbHandler);
        viewMvc.setRecyclerAdapter(adapter);
        //adapter.setLists(DBHandler.getInstance(getActivity().getApplicationContext()).getAllLists());

        return viewMvc.getRootView();
    }




    public void checkingIsAnyUpdateAction1()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("a").child(uid).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                saveDownloadTime();
                if(dataSnapshot.exists())
                {
                    long timestamp = (long) dataSnapshot.getValue();
                    if(!areTimestampUpdated(activity, timestamp))
                    {
                        Log.d("checking1", "something changed");
                        checkingForUpdatesAction(timestamp);
                    }
                    else
                    {
                        viewMvc.stopRefreshing();
                        Log.d("checking1", "nothing changed");
                        FirebaseDatabase.getInstance().goOffline();
                    }
                }
                else
                {
                    FirebaseDatabase.getInstance().goOffline();
                }

            }
        });
    }

    public static boolean areTimestampUpdated(Activity activity, long newTimestamp)
    {
        //Log.d("canICownload", "Checking");
        //Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastTimestamp = sharedPreferences.getLong("listsTimestamp", -1);

        return lastTimestamp == newTimestamp;
    }

    public static void increaseListTimestamp(Activity activity)
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastTimestamp = sharedPreferences.getLong("listsTimestamp", -1);
        setListsTimestamp(activity, lastTimestamp + 1);

    }

    public static void setListsTimestamp(Activity activity, long timestamp)
    {
        //Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("listsTimestamp", timestamp);
        editor.commit();
    }

    public boolean canIDownload()
    {
        //Log.d("canICownload", "Checking");
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastDownload = sharedPreferences.getLong("lastListsDownload", 0);
        if(date.getTime() - lastDownload > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //60000 milliseconds - 60 seconds
            return true;
        else if (lastDownload - date.getTime() > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //if somebody changes device time
            return true;
        else
            return ProductsFragment.wasRecentlyLogOut(activity, SECONDS_TO_NEXT_SELF_DOWNLOAD);

    }

    public void saveDownloadTime()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastListsDownload", date.getTime());
        editor.commit();
    }

    public void checkingForUpdatesAction(long listsTimestamp)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String userEmail = user.getEmail();



        FirebaseDatabase.getInstance().getReference().child("e").child(uid).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Set<String> lastKeys = dbHandler.getListKeysSet();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Set<String> newKeys = map.keySet();

                    boolean somethingToDelete = false;
                    for(String lastKey:lastKeys)
                    {
                        if(!newKeys.contains(lastKey))
                        {
                            //Delete list
                            int id = dbHandler.getListIdByKey(lastKey);
                            dbHandler.deleteList(id);
                            somethingToDelete = true;

                        }
                    }
                    boolean somethingToDownload = false;
                    for(String key:newKeys)
                    {
                        if(!lastKeys.contains(key))
                        {
                            downloading.add(true);
                        }
                    }
                    Task<Void>[]tasks = new Task[downloading.size()];
                    int i=0;
                    for(String key:newKeys)
                    {
                        if(lastKeys.contains(key))
                        {
                            //Can check for update or not
                            if(map.get(key).toString() != dbHandler.getListTittle(key))
                            {
                                dbHandler.updateListTittle(dbHandler.getListIdByKey(key), map.get(key).toString());
                            }
                        }
                        else
                        {

                            somethingToDownload = true;
                            //if(downloading.size() == 0)
                            //activity.showProgressDialog(getString(R.string.please_wait));
                            //downloading.add(true);
                            //downloadList(key, map.get(key).toString(), userEmail, dbHandler);
                            tasks[i++] = downloadListWithTimestampDownloading(key, userEmail, dbHandler, uid, map.get(key).toString());
                        }
                    }

                    if(!somethingToDownload)
                    {
                        setListsTimestamp(activity, listsTimestamp);
                        loadLists();
                        viewMvc.stopRefreshing();
                        if(!changingActivity)
                        {
                            FirebaseDatabase.getInstance().goOffline();
                            Log.d("connection", "goOffline");
                        }

                    }
                    else
                    {
                        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                            @Override
                            public void onSuccess(List<Object> objects) {
                                setListsTimestamp(activity, listsTimestamp);
                                Log.d("tasks", "All success");
                            }
                        });
                    }


                }
                else
                {
                    setListsTimestamp(activity, listsTimestamp);
                    dbHandler.deleteAllLists();
                    loadLists();
                    if(!changingActivity)
                    {
                        FirebaseDatabase.getInstance().goOffline();
                        Log.d("connection", "goOffline");
                    }
                }
            }
        });
    }

    public void resetTimer()
    {
        stopTimer.cancel();
        if(!activity.isOfflineModeOn())
            stopTimer.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        changingActivity = false;
        loadLists();

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

        if(!activity.isOfflineModeOn())
        {
            FirebaseDatabase.getInstance().goOnline();

            if(canIDownload())
                checkingIsAnyUpdateAction1();
        }


        if(adapter.getItemCount() == 0)
        {
            viewMvc.changeVisibilityOfNoListItem(true);
        }
        else
        {
            viewMvc.changeVisibilityOfNoListItem(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
        if(!changingActivity)
        {
            FirebaseDatabase.getInstance().goOffline();
            Log.d("connection", "goOffline onStop()");
        }

    }

    public Task<Void> downloadListWithTimestampDownloading(String listKey, String userEmail, DBHandler dbHandler, String userUid, String tittle)
    {
        TaskCompletionSource<String> createSource = new TaskCompletionSource<>();
        Task createTask = createSource.getTask();

        TaskCompletionSource<String> timestampSource = new TaskCompletionSource<>();
        Task timestampTask = timestampSource.getTask();

        TaskCompletionSource<String> ownerSource = new TaskCompletionSource<>();
        Task ownerTask = ownerSource.getTask();

        TaskCompletionSource<DataSnapshot> productsSource = new TaskCompletionSource<>();
        Task productsTask = productsSource.getTask();

        TaskCompletionSource<List<FriendModel>> friendsSource = new TaskCompletionSource<>();
        Task friendsTask = friendsSource.getTask();

        TaskCompletionSource<String> friendsTimestampSource = new TaskCompletionSource<>();
        Task friendsTimestampTask = friendsTimestampSource.getTask();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        reference.child("l").child(listKey).child("c").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                createSource.setResult(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createSource.setException(error.toException());
            }
        });

        reference.child("lu").child(listKey).child("o").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                ownerSource.setResult(dataSnapshot.getValue().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ownerSource.setException(e);
            }
        });


        reference.child("p").child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                productsSource.setResult(dataSnapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                productsSource.setException(e);
            }
        });



        reference.child("d").child(userUid).child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                timestampSource.setResult(dataSnapshot.getValue().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                timestampSource.setException(e);
            }
        });

        reference.child("lu").child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
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
                    else if(!uid.equals(userUid) && !uid.equals("a"))
                    {
                        String mail = map.get(uid).toString();
                        FriendModel fr = new FriendModel("", mail, true, false);
                        fr.setUid(uid);
                        //Log.d("NewMails", fr.getMail());
                        mails.add(fr);

                        //Log.d("friends", mail + " " + mail.length());
                    }
                }


                friendsSource.setResult(mails);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                friendsSource.setException(e);
            }
        });

        reference.child("f").child(userUid)
                .child(listKey).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                friendsTimestampSource.setResult(dataSnapshot.getValue().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                friendsTimestampSource.setException(e);
            }
        });





        return Tasks.whenAll(createTask, friendsTimestampTask, friendsTask, ownerTask, productsTask, timestampTask).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //String tittle = (String) tittleTask.getResult();
                String owner = (String) ownerTask.getResult();
                boolean amIOwner = owner.equals(userEmail);
                String downloadTimestamp = (String) timestampTask.getResult();
                String friendsTimestamp = (String) friendsTimestampTask.getResult();
                String createTimestamp = (String) createTask.getResult();
                List<FriendModel> friends = (List<FriendModel>) friendsTask.getResult();

                DataSnapshot snapshot = (DataSnapshot) productsTask.getResult();
                ListModel list;
                if(snapshot.exists())
                {
                    //Log.d("downloading", "downloading started");
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) snapshot.getValue();
                    Object[] mapKeys = map.keySet().toArray();
                    List<ProductModel> newProducts = new ArrayList<>();
                    int numberSelected = 0;
                    for(Object o:mapKeys)
                    {
                        //Log.d("downloading", o.toString());
                        Map<String, Object>productMap = (Map<String, Object>) map.get(o.toString());
                        int number = 1;
                        if(productMap.containsKey("n"))
                            number = Integer.parseInt(productMap.get("n").toString());
                        String description = "";
                        if(productMap.containsKey("d"))
                            description = productMap.get("d").toString();

                        boolean selected = Boolean.parseBoolean(productMap.get("s").toString());
                        if(selected)
                            numberSelected++;
                        int category = 0;
                        if(productMap.containsKey("c"))
                            category = Integer.parseInt(productMap.get("c").toString());

                        ProductModel product = new ProductModel(o.toString(), description, selected, number);
                        product.setCategoryId(category);
                        newProducts.add(product);
                        //Log.d("downloading", product.toString());
                    }

                    list = new ListModel(tittle, numberSelected, newProducts.size(), newProducts);

                }
                else
                {
                    list = new ListModel(tittle, 0, 0, new ArrayList<>());
                }

                //Log.d("downloaded with", downloadTimestamp);
                list.setFirebaseKey(listKey);
                list.setDownloadTimestamp(downloadTimestamp);
                list.setAmIOwner(amIOwner);
                list.setFriendsTimestamp("-1");
                dbHandler.insertList(list, createTimestamp);

                dbHandler.syncListFriends(friends, dbHandler.getListIdByKey(listKey), owner, friendsTimestamp, listKey);

                if(downloading.size() == 1)
                {

                    loadLists();

                    viewMvc.stopRefreshing();

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
                Log.d("downloading", "Downloading failed");
            }
        });



    }


    public void loadLists()
    {
        activity.showProgressDialog(getString(R.string.please_wait));
        adapter.setLists(dbHandler.getAllLists());
        if(adapter.getItemCount() == 0)
            showNoListImage();
        else
            hideNoListImage();

        activity.hideProgressDialog();
    }

    public void showNoListImage()
    {
        viewMvc.changeVisibilityOfNoListItem(true);
    }

    public void hideNoListImage()
    {
        viewMvc.changeVisibilityOfNoListItem(false);
    }

    public void scrollToTheStarting()
    {
        viewMvc.scrollToRecyclerTop();
    }

    public void setChangingActivityTrue()
    {
        changingActivity = true;
    }


    @Override
    public void onAddButtonClick() {
        ListCreatingActivity.launch(getContext());
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onNoListItemClick() {
        ListCreatingActivity.launch(getContext());
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onSwipeRefresh() {
        if(!activity.isOfflineModeOn() && canIDownload())
        {
            Log.d("refresh", "Downloading");
            FirebaseDatabase.getInstance().goOnline();
            checkingIsAnyUpdateAction1();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    viewMvc.stopRefreshing();
                }
            }, 5000);
        }
        else
        {
            Log.d("refresh", "Waiting");

            viewMvc.stopRefreshing();
        }
    }
}