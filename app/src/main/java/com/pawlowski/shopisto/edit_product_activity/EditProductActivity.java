package com.pawlowski.shopisto.edit_product_activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class EditProductActivity extends BaseActivity implements EditProductViewMvc.EditProductButtonsClickListener {

    private ProductModel product;
    private int listId;
    private String listTittle;
    private String listKey;
    private boolean groupProduct = false;
    private boolean editGroup = false;
    private int groupId;
    private String groupKey;

    private EditProductViewMvc viewMvc;
    private CategoryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new EditProductViewMvc(getLayoutInflater(), null);
        setContentView(viewMvc.getRootView());
        Toolbar toolbar = findViewById(R.id.toolbar_edit_product);

        setSupportActionBar(toolbar);

        FirebaseDatabase.getInstance().goOffline();


        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId", -1);
        listTittle = bundle.getString("listTittle", "");
        product = bundle.getParcelable("product");
        groupProduct = bundle.getBoolean("groups", false);
        groupId = bundle.getInt("group_id", -1);
        listKey = bundle.getString("listKey");

        int selectedCategory = bundle.getInt("category_id", 0);

        if(groupProduct)
        {
            if(product.getTittle().length() != 0)
            {
                editGroup = true;
            }
            groupKey = bundle.getString("groupKey");
        }

        viewMvc.bindProduct(product);

        adapter = new CategoryAdapter(this, selectedCategory);
        viewMvc.setRecyclerAdapter(adapter);

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

        if(viewMvc.getTittleInputText().length() == 0)
        {
            showErrorSnackbar(getString(R.string.first_put_product_tittle), true);
            return false;
        }

        if(viewMvc.getNumberInputText().length() == 0)
        {
            showErrorSnackbar(getString(R.string.first_put_product_number), true);
            return false;


        }

        String previousTittle = product.getTittle();
        product.setTittle(viewMvc.getTittleInputText());
        product.setDescription(viewMvc.getDescriptionInputText());
        product.setNumber(Integer.parseInt(viewMvc.getNumberInputText()));
        product.setCategoryId(adapter.getSelectedCategory());

        if(!groupProduct)
        {
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
            String text = speakResults.get(0);
            if(text.length() > 0)
            {
                viewMvc.setTittleInput(text);
            }
        }
        else if(requestCode == 3 && resultCode == RESULT_OK)
        {
            ArrayList<String> speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = speakResults.get(0);
            if(text.length() > 0)
            {
                viewMvc.setDescriptionInput(text);
            }
        }

    }

    @Override
    public void onMicTittleClick() {
        convertSpeech(2);
    }

    @Override
    public void onMicDescriptionClick() {
        convertSpeech(3);
    }
}