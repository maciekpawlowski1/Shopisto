package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.filters.MyFilters;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Locale;

public class EditProductActivity extends BaseActivity {

    ProductModel product;
    int listId;
    String listTittle;
    int selectedCategory;
    String listKey;

    EditText tittleEditText;
    EditText descriptionEditText;
    EditText numberEditText;

    Toolbar toolbar;
    boolean groupProduct = false;
    boolean editGroup = false;
    int groupId;
    String groupKey;

    ImageButton micTittle;
    ImageButton micDescription;

    RecyclerView categoryRecycler;
    CategoryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.cancel_icon2);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar = findViewById(R.id.toolbar_edit_product);
        //toolbar.setLogo(R.drawable.cancel_icon2);

        setSupportActionBar(toolbar);

        FirebaseDatabase.getInstance().goOffline();

        categoryRecycler = findViewById(R.id.category_recycler_edit_product);
        tittleEditText = findViewById(R.id.tittle_edit_edit_product);
        descriptionEditText = findViewById(R.id.description_edit_edit_product);
        numberEditText = findViewById(R.id.number_edit_edit_product);

        micTittle = findViewById(R.id.mic_button_edit_product);
        micDescription = findViewById(R.id.mic_button_edit_product2);



        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId", -1);
        listTittle = bundle.getString("listTittle", "");
        product = bundle.getParcelable("product");
        groupProduct = bundle.getBoolean("groups", false);
        groupId = bundle.getInt("group_id", -1);
        listKey = bundle.getString("listKey");

        selectedCategory = bundle.getInt("category_id", 0);
        //product.setSelected(bundle.getBoolean("productSelected", false));
        tittleEditText.setText(product.getTittle());

        if(groupProduct)
        {
            if(product.getTittle().length() != 0)
            {
                editGroup = true;
            }
            groupKey = bundle.getString("groupKey");
        }

        if(product.getDescription().equals(" "))
            descriptionEditText.setText("");
        else
            descriptionEditText.setText(product.getDescription());

        numberEditText.setText(product.getNumber()+"");

        micTittle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertSpeech(2);
            }
        });

        micDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertSpeech(3);
            }
        });

        adapter = new CategoryAdapter(this, selectedCategory);
        categoryRecycler.setAdapter(adapter);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));

        tittleEditText.setFilters(new InputFilter[]{MyFilters.getTittleInputFilter()});
        descriptionEditText.setFilters(new InputFilter[]{MyFilters.getTittleInputFilter()});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        else if(item.getItemId() == R.id.finish_edit_menu)
        {
            if(save())
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!groupProduct)
        {
            Intent i = new Intent(EditProductActivity.this, ListActivity.class);
            i.putExtra("listId", listId);
            i.putExtra("listTittle", listTittle);
            i.putExtra("listKey", listKey);
            startActivity(i);
            finish();
        }

        super.onBackPressed();
    }

    private boolean save()
    {

        if(tittleEditText.getText().toString().length() == 0)
        {
            showErrorSnackbar(getString(R.string.first_put_product_tittle), true);
            return false;
        }

        if(numberEditText.getText().toString().length() == 0)
        {
            showErrorSnackbar(getString(R.string.first_put_product_number), true);
            return false;


        }

        String previousTittle = product.getTittle();
        product.setTittle(tittleEditText.getText().toString());
        product.setDescription(descriptionEditText.getText().toString());
        product.setNumber(Integer.parseInt(numberEditText.getText().toString()));
        product.setCategoryId(adapter.getSelectedCategory());

        if(!groupProduct)
        {
            //Log.d("previous", previousTittle);
            if(!isOfflineModeOn())
                OnlineDBHandler.updateProduct(listKey, product, !product.getTittle().equals(previousTittle), previousTittle,
                        DBHandler.getInstance(getApplicationContext()).getFriendsWithoutNicknamesFromThisList(listId));

            DBHandler.getInstance(getApplicationContext()).updateProduct(product);
        }
        else
        {

            if(editGroup)
            {
                if(!isOfflineModeOn())
                    OnlineDBHandler.updateProductInGroup(product, groupKey, !product.getTittle().equals(previousTittle), previousTittle);
                DBHandler.getInstance(getApplicationContext()).updateProductInGroup(product);
            }
            else
            {
                if(!isOfflineModeOn())
                    OnlineDBHandler.updateProductInGroup(product, groupKey, false, "");
                DBHandler.getInstance(getApplicationContext()).insertProductToGroup(product, groupId);
            }
            DBHandler.getInstance(getApplicationContext()).increaseGroupTimestamp(groupKey);
        }

        return true;
    }


    void convertSpeech(int code)
    {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        startActivityForResult(i, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK)
        {
            ArrayList<String> speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //showErrorSnackbar(speakResults.get(0), false);
            String tittle = speakResults.get(0);
            if(tittle.length() > 0)
            {
                tittleEditText.setText(tittle);
            }
        }
        else if(requestCode == 3 && resultCode == RESULT_OK)
        {
            ArrayList<String> speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String tittle = speakResults.get(0);
            if(tittle.length() > 0)
            {
                descriptionEditText.setText(tittle);
            }
        }

    }
}