package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.main.ProductsFragment;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private static final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 300;
    ImageButton imageButton;
    CardView cardView;
    int listId;
    String listTittle;
    String listKey;

    RecyclerView recycler;
    FriendsAdapter adapter;
    TextView textView;
    CountDownTimer goOfflineTimer;

    boolean amIOwner;
    boolean changingActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        getSupportActionBar().setTitle(getString(R.string.share_list));

        goOfflineTimer = new CountDownTimer(10000, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(!changingActivity)
                {
                    FirebaseDatabase.getInstance().goOffline();
                }

            }
        };

        if(canIDownload())
        {
            FirebaseDatabase.getInstance().goOnline();
            OnlineDBHandler.downloadAllFriendsAndSync(DBHandler.getInstance(getApplicationContext()),
                    DBHandler.getInstance(getApplicationContext()).getAllFriends(), new OnlineDBHandler.ActionWhenSuccess() {
                        @Override
                        public void action() {
                            saveDownloadTime();
                            resetTimer();
                            loadFriends();
                        }
                    });
        }


        imageButton = findViewById(R.id.image_button_share);
        cardView = findViewById(R.id.card_view_share);

        recycler = findViewById(R.id.friends_recycler_share);
        textView = findViewById(R.id.text_view_share_activity);

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle");
        listKey = bundle.getString("listKey");

        amIOwner = DBHandler.getInstance(getApplicationContext()).amIListOwner(listId);
        //Log.d("owner", amIOwner+"");

        if(amIOwner)
        {
            textView.setText(R.string.share_friends_from_shopisto);
        }
        else
        {
            textView.setText(R.string.another_list_users);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAction();
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAction();

            }
        });

        adapter = new FriendsAdapter(this, listKey, amIOwner);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }


    @Override
    protected void onStart() {
        super.onStart();
        changingActivity = false;

        loadFriends();

    }

    public void loadFriends()
    {
        if(amIOwner)
            adapter.setFriends(DBHandler.getInstance(getApplicationContext()).getAllFriendsToShareActivity(listId));
        else
        {
            adapter.setFriends(DBHandler.getInstance(getApplicationContext()).getFriendsWithoutNicknamesFromThisList(listId));
        }
    }

    public boolean canIDownload()
    {
        //Log.d("canICownload", "Checking");
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        long lastDownload = sharedPreferences.getLong("lastAllFriendsDownload", 0);
        if(date.getTime() - lastDownload > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //60000 milliseconds - 60 seconds
            return true;
        else if (lastDownload - date.getTime() > (SECONDS_TO_NEXT_SELF_DOWNLOAD * 1000)) //if somebody changes device time
            return true;
        else
            return ProductsFragment.wasRecentlyLogOut(this, SECONDS_TO_NEXT_SELF_DOWNLOAD);

    }

    public void saveDownloadTime()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastAllFriendsDownload", date.getTime());
        editor.commit();
    }

    public void resetTimer()
    {
        goOfflineTimer.cancel();
        goOfflineTimer.start();
    }

    private void shareAction()
    {
        /*Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");

        List<ProductModel> products = DBHandler.getInstance(getApplicationContext()).getAllProductOfList(listId);


        String shareBody = getBody(products);
        String shareSub = listTittle;
        myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(myIntent, getString(R.string.share_using)));*/
        shareAction(listTittle, listId, ShareActivity.this);
    }

    public static void shareAction(String listTittle, int listId, Activity activity)
    {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");

        List<ProductModel> products = DBHandler.getInstance(activity. getApplicationContext()).getAllProductOfList(listId);


        String shareBody = getBody(products);
        String shareSub = listTittle;
        myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        activity.startActivity(Intent.createChooser(myIntent, activity.getString(R.string.share_using)));
    }

    public static String getBody(List<ProductModel> products)
    {
        String body = "";
        for(int i=0;i<products.size();i++)
        {
            ProductModel product = products.get(i);
            body+= (i+1) + ") " + product.getTittle();
            if(product.getDescription().length() != 0 && !product.getDescription().equals(" "))
            {
                body += " (" + product.getDescription() + ")";
            }
            body += " - " + product.getNumber();

            body+="\n";
        }
        return body;
    }

    public int getListId()
    {
        return listId;
    }

    public String getListTittle() {
        return listTittle;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.add_friend_share_menu:
                Intent i = new Intent(ShareActivity.this, AddFriendActivity.class);
                i.putExtra("listId", listId);
                i.putExtra("listTittle", listTittle);
                i.putExtra("listKey", listKey);
                changingActivity = true;
                startActivity(i);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(ShareActivity.this, ListActivity.class);

        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        changingActivity = true;
        startActivity(i);
        finish();

    }
}