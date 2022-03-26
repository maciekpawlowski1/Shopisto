package com.pawlowski.shopisto.add_group_activity;

import android.content.Intent;
import android.os.Bundle;

import com.pawlowski.shopisto.GroupActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.GroupModel;

import java.util.Calendar;

public class AddGroupActivity extends BaseActivity implements AddGroupViewMvc.AddGroupButtonsClickListener {

    private AddGroupViewMvc viewMvc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new AddGroupViewMvc(getLayoutInflater(), null);
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
            DBHandler db = DBHandler.getInstance(getApplicationContext());
            GroupModel group = new GroupModel(tittle);
            group.setKey(key);
            db.insertGroup(group, Calendar.getInstance().getTime().getTime()+"");
            int id = db.getIdOfLastGroup();
            Intent i = new Intent(AddGroupActivity.this, GroupActivity.class);
            i.putExtra("groupTittle", tittle);
            i.putExtra("groupId", id);
            i.putExtra("groupKey", key);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }
        else
        {
            showErrorSnackbar(getString(R.string.first_put_group_tittle), true);
        }
    }
}