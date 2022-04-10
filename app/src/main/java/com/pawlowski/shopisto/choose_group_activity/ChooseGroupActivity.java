package com.pawlowski.shopisto.choose_group_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.add_group_activity.AddGroupActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.main.ProductGroupsAdapter;
import com.pawlowski.shopisto.share_activity.ShareActivity;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class ChooseGroupActivity extends BaseActivity implements ChooseGroupViewMvc.ChooseGroupButtonsClickListener {

    private String listKey;

    private ProductGroupsAdapter adapter;
    private ChooseGroupViewMvc viewMvc;
    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int listId, String listKey)
    {
        Intent i = new Intent(context, ChooseGroupActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listKey", listKey);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newChooseGroupViewMvcInstance(null);
        setContentView(viewMvc.getRootView());
        getSupportActionBar().setTitle(getString(R.string.choose_group));

        Bundle bundle = getIntent().getExtras();
        int listId = bundle.getInt("listId", -1);
        listKey = bundle.getString("listKey", "");

        adapter = new ProductGroupsAdapter(this, true, listId, null, dbHandler);
        viewMvc.setRecyclerAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        adapter.setGroups(dbHandler.getAllGroups());
        if(adapter.getItemCount() == 0)
        {
            viewMvc.showNoGroupsImages();
        }
        else
        {
            viewMvc.hideNoGroupsImages();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    public void navigateToCreatingGroupAction()
    {
        AddGroupActivity.launch(this);
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


    @Override
    public void onAddGroupClickListener() {
        navigateToCreatingGroupAction();
    }
}