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

public class AddProductsToListActivity extends BaseActivity implements AddProductsToListViewMvc.AddProductsToListButtonsClickListener {


    private AddedProductsAdapter adapter;
    private int listId;
    private String listKey;
    private List<FriendModel> friendsFromList = new ArrayList<>();
    private CountDownTimer stopTimer;
    private boolean changingActivity = false;
    private boolean offlineMode = false;

    private AddProductsToListViewMvc viewMvc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new AddProductsToListViewMvc(getLayoutInflater(), null);
        setContentView(viewMvc.getRootView());

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listKey = bundle.getString("listKey");

        offlineMode = isOfflineModeOn();


        //FirebaseDatabase.getInstance().goOnline();

        adapter = new AddedProductsAdapter(this, listId, listKey, offlineMode);
        viewMvc.setRecyclerAdapter(adapter);
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
        viewMvc.resetProductTittleInput();
        adapter.turnOffSuggesting();
        ProductModel newProduct = new ProductModel(tittle, " ", false, 1);
        adapter.addProductAtBegining(newProduct);
        viewMvc.scrollRecyclerToTheTop();
        DBHandler.getInstance(getApplicationContext()).insertProduct(newProduct, listId);
        newProduct.setId(DBHandler.getInstance(getApplicationContext()).getIdOfLastProduct());
    }

    public void resetTimer()
    {
        stopTimer.cancel();
        stopTimer.start();

    }


    private void convertSpeech()
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
            String tittle = speakResults.get(0);
            if(!adapter.isAnyTittleWithThisTittle(tittle))
                addProduct(tittle);
            else
            {
                viewMvc.resetProductTittleInput();
                adapter.turnOffSuggesting();
            }
        }

    }


    @Override
    public void onBackPressed() {

        changingActivity = true;

        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        changingActivity = false;
        loadFriendsFromList();

        FirebaseDatabase.getInstance().goOffline();

    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);

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


    @Override
    public void onProductTittleTextChanged(String tittleInput) {
        if(tittleInput.length() != 0)
            adapter.turnOnSuggesting(tittleInput);
        else
        {
            adapter.turnOffSuggesting();
        }
    }

    @Override
    public void onBackButtonClick() {
        onBackPressed();
    }

    @Override
    public void onMicButtonClick() {
        convertSpeech();
    }
}