package com.pawlowski.shopisto.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.HelpActivity;
import com.pawlowski.shopisto.MyFragmentHolder;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.database.DBHandler;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BaseActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    MyFragmentHolder fragmentHolder = null;
    TextView mailInHeader;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout=findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();




        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                Fragment fragment = null;
                switch (id)
                {
                    case R.id.shopping_lists_menu:
                        if(fragmentHolder != null)
                            fragmentHolder.setChangingActivityTrue();
                        fragment = new ShoppingListsFragment(MainActivity.this);
                        fragmentHolder = (MyFragmentHolder) fragment;
                        loadFragment(fragment);
                        break;
                    case R.id.products_menu:
                        if(fragmentHolder != null)
                            fragmentHolder.setChangingActivityTrue();
                        fragment = new ProductsFragment(MainActivity.this);
                        fragmentHolder = (MyFragmentHolder) fragment;
                        loadFragment(fragment);
                        break;
                    case R.id.restore_menu:
                        fragment = new RestoreFragment(MainActivity.this);
                        fragmentHolder = null;
                        loadFragment(fragment);

                        break;
                    case R.id.log_out_menu:
                        saveLogOutTime();
                        resetListsTimestamp(MainActivity.this);
                        FirebaseAuth.getInstance().signOut();
                        DBHandler.getInstance(getApplicationContext()).deleteEverything();
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        finish();
                        break;
                    case R.id.help_navigation_menu:
                        fragmentHolder = null;
                        Intent i2 = new Intent(MainActivity.this, HelpActivity.class);
                        startActivity(i2);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        break;
                    default:

                        return true;
                }
                return true;
            }
        });
        navigationView.setCheckedItem(R.id.shopping_lists_menu);
        fragmentHolder = new ShoppingListsFragment(MainActivity.this);
        loadFragment(fragmentHolder);


        View headerView = navigationView.getHeaderView(0);
        mailInHeader = headerView.findViewById(R.id.mail_text_header);


        mAdView = findViewById(R.id.ad_view_main_activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }

    public static void resetListsTimestamp(Activity activity)
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("listsTimestamp", 0);
        editor.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(!isOfflineModeOn()) {
            mailInHeader.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            navigationView.getMenu().findItem(R.id.log_out_menu).setVisible(true);

        }
        else
        {
            mailInHeader.setText(R.string.not_logged_in);
            mailInHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    //finish();
                }
            });


            navigationView.getMenu().findItem(R.id.log_out_menu).setVisible(false);

        }
    }

    private void loadFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
        fragmentTransaction.addToBackStack(null);
    }

    public void saveLogOutTime()
    {
        Date date = Calendar.getInstance().getTime();
        SharedPreferences sharedPreferences = getSharedPreferences("lastDownloadPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastLogOut", date.getTime());
        editor.commit();
    }


    @Override
    public void onBackPressed() {
        if(!drawerLayout.isDrawerOpen(Gravity.LEFT))
            drawerLayout.openDrawer(Gravity.LEFT);
        else
        {
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.help_help_menu:
                Intent i = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}