package com.pawlowski.shopisto.add_products_to_list_activity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class AddedProductsNewItemViewMvc extends BaseObservableViewMvc<AddedProductsNewItemViewMvc.AddedProductsNewItemButtonsClickListener> {
    private final TextView tittleSuggestionText;
    private final ConstraintLayout constraintSuggestions;
    private final Button addButtonSuggestions;

    private String currentText;
    AddedProductsNewItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.added_product_new_card, viewGroup, false);

        tittleSuggestionText = findViewById(R.id.tittle_added_new_card);
        constraintSuggestions = findViewById(R.id.constraint_added_new_card);
        addButtonSuggestions = findViewById(R.id.add_added_new_card);

        constraintSuggestions.setOnClickListener(v -> {
            for(AddedProductsNewItemButtonsClickListener l:listeners)
            {
                l.onSuggestionItemClick(currentText);
            }
        });

        addButtonSuggestions.setOnClickListener(v -> {
            for(AddedProductsNewItemButtonsClickListener l:listeners)
            {
                l.onSuggestionItemClick(currentText);
            }
        });
    }

    public void bindItem(String tittleString, String searchedText)
    {
        this.currentText = tittleString;

        SpannableStringBuilder s = new SpannableStringBuilder(tittleString);
        int fragmentStart = tittleString.toLowerCase().indexOf(searchedText.toLowerCase());
        if(fragmentStart != -1)
            s.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), fragmentStart, fragmentStart+searchedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tittleSuggestionText.setText(s);
    }

    interface AddedProductsNewItemButtonsClickListener
    {
        void onSuggestionItemClick(String tittleString);
    }
}
