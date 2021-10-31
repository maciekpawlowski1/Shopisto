package com.pawlowski.shopisto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.main.ShoppingListsFragment;
import com.pawlowski.shopisto.models.ListModel;

import java.util.Calendar;

public class ListCreatingActivity extends BaseActivity {

    TextInputEditText tittleInput;
    Button createButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_creating);

        getSupportActionBar().setTitle(getString(R.string.create_list));

        tittleInput = findViewById(R.id.tittle_input_list_creating);
        createButton = findViewById(R.id.create_button_list_creating);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tittle = tittleInput.getText().toString();
                if(tittle.length() != 0)
                {
                    DBHandler db = DBHandler.getInstance(getApplicationContext());
                    String key = "offline";
                    if(!isOfflineModeOn())
                        key = OnlineDBHandler.addNewList(tittle);
                    ShoppingListsFragment.increaseListTimestamp(ListCreatingActivity.this);
                    ListModel newList = new ListModel(tittle, 0, 0, null);
                    newList.setFirebaseKey(key);
                    db.insertList(newList, Calendar.getInstance().getTime().getTime()+"");
                    int id = db.getIdOfLastList();
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