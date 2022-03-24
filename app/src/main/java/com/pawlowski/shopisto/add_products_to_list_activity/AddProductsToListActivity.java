package com.pawlowski.shopisto.add_products_to_list_activity;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.filters.MyFilters;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddProductsToListActivity extends BaseActivity {


    AddedProductsAdapter adapter;
    int listId;
    String listKey;
    ScrollView scrollView;
    List<FriendModel> friendsFromList = new ArrayList<>();

    boolean somethingAdded = false;

    CountDownTimer stopTimer;

    boolean changingActivity = false;

    boolean offlineMode = false;

    ImageButton backButton;
    ImageButton micButton;
    //Button addButton;
    RecyclerView recyclerView;
    EditText tittleEditText;




    //ArrayList<ProductModel> addedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products_to_list);

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listKey = bundle.getString("listKey");

        backButton = findViewById(R.id.back_button_add_products_to_list);
        micButton = findViewById(R.id.mic_button_add_products_to_list);
        recyclerView = findViewById(R.id.recycler_added_products);
        //addButton = findViewById(R.id.add_button_add_product_to_list);
        tittleEditText = findViewById(R.id.tittle_edit_text_add_product_to_list);
        scrollView = findViewById(R.id.scroll_view_add_products_to_list);

        offlineMode = isOfflineModeOn();


        //FirebaseDatabase.getInstance().goOnline();

        /*tittleEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {


                return false;
            }
        });*/

        InputFilter filter = MyFilters.getTittleInputFilter();
        tittleEditText.setFilters(new InputFilter[] {filter});

        tittleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //showPopUp();
                if(s.toString().length() != 0)
                    adapter.turnOnSuggesting(s.toString());
                else
                {
                    adapter.turnOffSuggesting();
                }
           }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tittle = tittleEditText.getText().toString();
                if(tittle.length() == 0)
                {
                    showErrorSnackbar(getString(R.string.first_put_product_tittle), true);
                }
                else
                {
                    addProduct(tittle);
                }
            }
        });*/

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertSpeech();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new AddedProductsAdapter(this, listId, listKey, offlineMode);
        recyclerView.setAdapter(adapter);
        adapter.setProducts(DBHandler.getInstance(getApplicationContext()).getAllProductOfList(listId));





        stopTimer = new CountDownTimer(10000, 6000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if(!changingActivity)
                    FirebaseDatabase.getInstance().goOffline();

            }
        }.start();


    }

    void addProduct(String productTittle)
    {
        resetTimer();
        FirebaseDatabase.getInstance().goOnline();
        String tittle = productTittle.trim();
        if(!offlineMode)
            OnlineDBHandler.addProductWithoutDescription(listKey, tittle, 1, friendsFromList);
        tittleEditText.setText("");
        adapter.turnOffSuggesting();
        ProductModel newProduct = new ProductModel(tittle, " ", false, 1);
        adapter.addProductAtBegining(newProduct);
        recyclerView.scrollToPosition(0);
        //adapter.setProducts(addedProducts);
        DBHandler.getInstance(getApplicationContext()).insertProduct(newProduct, listId);
        newProduct.setId(DBHandler.getInstance(getApplicationContext()).getIdOfLastProduct());
        //addedProducts.add(0, newProduct);
    }

    public void resetTimer()
    {
        stopTimer.cancel();
        stopTimer.start();

    }







    void convertSpeech()
    {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK)
        {
            ArrayList<String> speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //showErrorSnackbar(speakResults.get(0), false);
            String tittle = speakResults.get(0);
            if(!adapter.isAnyTittleWithThisTittle(tittle))
                addProduct(tittle);
            else
            {
                tittleEditText.setText("");
                adapter.turnOffSuggesting();
            }
        }

    }


    @Override
    public void onBackPressed() {
        /*DBHandler dbHandler = DBHandler.getInstance(getApplicationContext());
        for(ProductModel p:addedProducts)
        {
            dbHandler.insertProduct(p, listId);
        }*/

        changingActivity = true;

        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        changingActivity = false;
        loadFriendsFromList();

        FirebaseDatabase.getInstance().goOffline();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer.cancel();
        if(!changingActivity)
            FirebaseDatabase.getInstance().goOffline();

    }

    public void loadFriendsFromList()
    {
        friendsFromList = DBHandler.getInstance(getApplicationContext()).getFriendsWithoutNicknamesFromThisList(listId);
    }



    public List<FriendModel> getFriendsFromList()
    {
        return friendsFromList;
    }






}