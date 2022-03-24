package com.pawlowski.shopisto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.GroupModel;

import java.util.Calendar;

public class AddGroupActivity extends BaseActivity {

    TextInputEditText tittleInput;
    Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        getSupportActionBar().setTitle(getString(R.string.create_group));

        tittleInput = findViewById(R.id.tittle_input_group_creating);
        createButton = findViewById(R.id.create_button_group_creating);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tittle = tittleInput.getText().toString();
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
        });
    }
}