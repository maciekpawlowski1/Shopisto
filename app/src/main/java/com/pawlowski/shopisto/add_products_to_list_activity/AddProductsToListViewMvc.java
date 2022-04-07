package com.pawlowski.shopisto.add_products_to_list_activity;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.filters.MyFilters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddProductsToListViewMvc extends BaseObservableViewMvc<AddProductsToListViewMvc.AddProductsToListButtonsClickListener> {

    private final ImageButton backButton;
    private final ImageButton micButton;
    private final RecyclerView recyclerView;
    private final EditText tittleEditText;
    private final ScrollView scrollView;



    public AddProductsToListViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_add_products_to_list, viewGroup, false);
        backButton = findViewById(R.id.back_button_add_products_to_list);
        micButton = findViewById(R.id.mic_button_add_products_to_list);
        recyclerView = findViewById(R.id.recycler_added_products);
        tittleEditText = findViewById(R.id.tittle_edit_text_add_product_to_list);
        scrollView = findViewById(R.id.scroll_view_add_products_to_list);

        InputFilter filter = MyFilters.getTittleInputFilter();
        tittleEditText.setFilters(new InputFilter[] {filter});


        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddProductsToListButtonsClickListener l:listeners)
                {
                    l.onMicButtonClick();
                }
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddProductsToListButtonsClickListener l:listeners)
                {
                    l.onBackButtonClick();
                }
            }
        });

        tittleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for(AddProductsToListButtonsClickListener l:listeners)
                {
                    l.onProductTittleTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void scrollRecyclerToTheTop()
    {
        recyclerView.scrollToPosition(0);
    }

    public void setRecyclerAdapter(AddedProductsAdapter adapter)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void resetProductTittleInput()
    {
        tittleEditText.setText("");
    }

    interface AddProductsToListButtonsClickListener
    {
        void onProductTittleTextChanged(String tittleInput);
        void onBackButtonClick();
        void onMicButtonClick();
    }
}
