package com.pawlowski.shopisto.share_activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.add_friend_activity.AddFriendActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.main.products_fragment.ProductsFragment;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ShareActivity extends BaseActivity implements ShareActivityViewMvc.ShareActivityButtonsClickListener {

    private static final int SECONDS_TO_NEXT_SELF_DOWNLOAD = 300;

    int listId;
    String listTittle;
    String listKey;

    FriendsAdapter adapter;
    CountDownTimer goOfflineTimer;

    boolean amIOwner;
    boolean changingActivity = false;

    private ShareActivityViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int listId, String listTittle, String listKey)
    {
        Intent i = new Intent(context, ShareActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newShareActivityViewMvcInstance(null);
        setContentView(viewMvc.getRootView());

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
            OnlineDBHandler.downloadAllFriendsAndSync(dbHandler,
                    dbHandler.getAllFriends(), new OnlineDBHandler.ActionWhenSuccess() {
                        @Override
                        public void action() {
                            saveDownloadTime();
                            resetTimer();
                            loadFriends();
                        }
                    });
        }




        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle");
        listKey = bundle.getString("listKey");

        amIOwner = dbHandler.amIListOwner(listId);
        //Log.d("owner", amIOwner+"");

        if(amIOwner)
        {
            viewMvc.setTextViewText(getString(R.string.share_friends_from_shopisto));
        }
        else
        {
           viewMvc.setTextViewText(getString(R.string.another_list_users));
        }



        adapter = new FriendsAdapter(this, listKey, amIOwner);
        viewMvc.setAdapter(adapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        changingActivity = false;

        loadFriends();

    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    public void loadFriends()
    {
        if(amIOwner)
            adapter.setFriends(dbHandler.getAllFriendsToShareActivity(listId));
        else
        {
            adapter.setFriends(dbHandler.getFriendsWithoutNicknamesFromThisList(listId));
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
        shareAction(listTittle, listId, ShareActivity.this);
    }

    public static void shareAction(String listTittle, int listId, Activity activity)
    {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");

        List<ProductModel> products = DBHandler.getInstance(activity.getApplicationContext()).getAllProductOfList(listId);


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
            body += (i+1) + ") " + product.getTittle();
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

    @Override
    public void onImageButtonClick() {
        shareAction();
    }

    @Override
    public void onCardClick() {
        shareAction();
    }
}