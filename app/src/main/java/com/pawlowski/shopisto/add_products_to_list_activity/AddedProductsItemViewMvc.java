package com.pawlowski.shopisto.add_products_to_list_activity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.models.ProductModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddedProductsItemViewMvc extends BaseObservableViewMvc<AddedProductsItemViewMvc.AddedProductsItemButtonsClickListener> {
    private final ImageButton plusButton;
    private final ImageButton trashOrMinusButton;
    private final TextView tittleText;
    private final TextView numberText;

    private ProductModel currentProduct;
    private int currentPosition;

    AddedProductsItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.added_product_card, viewGroup, false);

        plusButton = findViewById(R.id.plus_image_button_added_card);
        trashOrMinusButton = findViewById(R.id.trash_or_minus_image_button_added_card);
        tittleText = findViewById(R.id.tittle_added_card);
        numberText = findViewById(R.id.number_added_card);

        plusButton.setOnClickListener(v -> {
            for(AddedProductsItemButtonsClickListener l: listeners)
            {
                l.onPlusButtonClick(currentProduct, currentPosition, AddedProductsItemViewMvc.this);
            }
        });

        trashOrMinusButton.setOnClickListener(v -> {
            for(AddedProductsItemButtonsClickListener l: listeners)
            {
                l.onTrashOrMinusClick(currentProduct, currentPosition, AddedProductsItemViewMvc.this);
            }
        });
    }

    public void bindProduct(ProductModel currentProduct, int position, boolean suggesting, String searchedText)
    {
        this.currentProduct = currentProduct;
        this.currentPosition = position;

        if(suggesting)
        {
            String tittleString = currentProduct.getTittle();
            SpannableStringBuilder s = new SpannableStringBuilder(tittleString);
            int fragmentStart = tittleString.toLowerCase().indexOf(searchedText.toLowerCase());
            if(fragmentStart != -1)
                s.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), fragmentStart, fragmentStart+searchedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tittleText.setText(s);
        }
        else
        {
            tittleText.setText(currentProduct.getTittle());
        }

        numberText.setText(currentProduct.getNumber()+"");
        if(currentProduct.getNumber() == 1)
        {
            trashOrMinusButton.setImageResource(R.drawable.delete_icon);
        }
        else
        {
            trashOrMinusButton.setImageResource(R.drawable.minus_icon);
        }
    }

    interface AddedProductsItemButtonsClickListener {
        void onPlusButtonClick(ProductModel currentProduct, int currentPosition, AddedProductsItemViewMvc viewMvc);
        void onTrashOrMinusClick(ProductModel currentProduct, int currentPosition, AddedProductsItemViewMvc viewMvc);

    }
}
