package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pawlowski.shopisto.add_group_activity.AddGroupActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.main.ProductGroupsAdapter;

public class ChooseGroupActivity extends BaseActivity {

    RecyclerView recyclerView;
    ProductGroupsAdapter adapter;
    int listId;
    String listKey;
    ImageView image;
    TextView text;
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);
        getSupportActionBar().setTitle(getString(R.string.choose_group));

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId", -1);
        listKey = bundle.getString("listKey", "");

        recyclerView = findViewById(R.id.recycler_choose_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductGroupsAdapter(this, true, listId, null);
        recyclerView.setAdapter(adapter);



        image = findViewById(R.id.image_choose_group_activity);
        text = findViewById(R.id.text_choose_group_activity);
        button = findViewById(R.id.add_button_choose_group_activity);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreatingGroupAction();
            }
        });

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreatingGroupAction();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreatingGroupAction();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.setGroups(DBHandler.getInstance(getApplicationContext()).getAllGroups());
        if(adapter.getItemCount() == 0)
        {
            showNoGroupsImages();
        }
        else
        {
            hideNoGroupsImages();
        }
    }

    public void navigateToCreatingGroupAction()
    {
        Intent i = new Intent(ChooseGroupActivity.this, AddGroupActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
    public String getListKey()
    {
        return listKey;
    }

    public void hideNoGroupsImages()
    {
        image.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
    }

    public void showNoGroupsImages()
    {
        image.setVisibility(View.VISIBLE);
        text.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
    }
}