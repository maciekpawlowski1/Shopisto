package com.pawlowski.shopisto.list_creating_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.main.shopping_lists_fragment.ShoppingListsFragment;
import com.pawlowski.shopisto.models.ListModel;

import java.util.Calendar;

import javax.inject.Inject;

public class ListCreatingActivity extends BaseActivity {

    TextInputEditText tittleInput;
    Button createButton;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context)
    {
        Intent i = new Intent(context, ListCreatingActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_creating);
        getPresentationComponent().inject(this);

        getSupportActionBar().setTitle(getString(R.string.create_list));

        tittleInput = findViewById(R.id.tittle_input_list_creating);
        createButton = findViewById(R.id.create_button_list_creating);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tittle = tittleInput.getText().toString();
                if(tittle.length() != 0)
                {
                    String key = "offline";
                    if(!isOfflineModeOn())
                        key = OnlineDBHandler.addNewList(tittle);
                    ShoppingListsFragment.increaseListTimestamp(ListCreatingActivity.this);
                    ListModel newList = new ListModel(tittle, 0, 0, null);
                    newList.setFirebaseKey(key);
                    dbHandler.insertList(newList, Calendar.getInstance().getTime().getTime()+"");
                    int id = dbHandler.getIdOfLastList();
                    Intent i = new Intent(ListCreatingActivity.this, ListActivity.class);
                    i.putExtra("listTittle", tittle);
                    i.putExtra("listId", id);
                    i.putExtra("listKey", key);
                    i.putExtra("justCreated", true);
                    i.putExtra("amIOwner", true);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish();
                }
                else
                {
                    showErrorSnackbar(getString(R.string.first_put_tittle), true);
                }
            }
        });
    }



}