package com.pawlowski.shopisto.add_group_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.group_activity.GroupActivity;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.share_activity.ShareActivity;

import java.util.Calendar;

import javax.inject.Inject;

public class AddGroupActivity extends BaseActivity implements AddGroupViewMvc.AddGroupButtonsClickListener {

    private AddGroupViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context)
    {
        Intent i = new Intent(context, AddGroupActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newAddGroupViewMvcInstance(null);
        setContentView(viewMvc.getRootView());

        getSupportActionBar().setTitle(getString(R.string.create_group));
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    @Override
    public void onCreateButtonClick() {
        String tittle = viewMvc.getTittleInputText();
        if(tittle.length() != 0)
        {
            String key = "offline";
            if(!isOfflineModeOn())
                key = OnlineDBHandler.insertNewGroup(tittle);
            GroupModel group = new GroupModel(tittle);
            group.setKey(key);
            dbHandler.insertGroup(group, Calendar.getInstance().getTime().getTime()+"");
            int id = dbHandler.getIdOfLastGroup();

            GroupActivity.launch(this, id, tittle, key);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }
        else
        {
            showErrorSnackbar(getString(R.string.first_put_group_tittle), true);
        }
    }
}