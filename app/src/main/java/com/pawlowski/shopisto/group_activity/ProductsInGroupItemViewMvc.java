package com.pawlowski.shopisto.group_activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.models.ProductModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class ProductsInGroupItemViewMvc extends BaseObservableViewMvc<ProductsInGroupItemViewMvc.ProductsInGroupItemButtonsClickListener> {
    private final TextView tittleText;
    private final TextView descriptionText;
    private final TextView numberText;
    private final ImageView imageView;
    private final CardView cardView;

    private boolean isChoosing = false;
    private int currentPosition;
    private ProductModel currentProduct;

    ProductsInGroupItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.product_in_group_card, viewGroup, false);

        tittleText = findViewById(R.id.tittle_group_product_card);
        descriptionText = findViewById(R.id.description_group_product_card);
        numberText = findViewById(R.id.number_group_product_card);
        imageView = findViewById(R.id.image_group_product_card);
        cardView = findViewById(R.id.card_view_product_in_group_card);

        cardView.setOnLongClickListener(v -> {
            if(!isChoosing)
            {
                for(ProductsInGroupItemButtonsClickListener l:listeners)
                {
                    l.onCardLongClick(currentPosition);
                }
                return true;
            }
            else
                return false;
        });

        cardView.setOnClickListener(v -> {
            for(ProductsInGroupItemButtonsClickListener l:listeners)
            {
                l.onCardClick(currentProduct, currentPosition);
            }
        });
    }

    public void bindProduct(ProductModel currentProduct, boolean isSelected, boolean isChoosing, int position)
    {
        this.currentProduct = currentProduct;
        this.isChoosing = isChoosing;
        this.currentPosition = position;

        tittleText.setText(currentProduct.getTittle());
        descriptionText.setText(currentProduct.getDescription());
        numberText.setText(currentProduct.getNumber()+"");


        if(isSelected)
        {
            cardView.setCardBackgroundColor(rootView.getResources().getColor(R.color.selected_color));
        }
        else
        {
            cardView.setCardBackgroundColor(rootView.getResources().getColor(R.color.white));
        }

        ProductsInGroupAdapter.changeCategoryImageDependingOnCategory(imageView, currentProduct.getCategoryId());

    }

    interface ProductsInGroupItemButtonsClickListener {
        void onCardClick(ProductModel currentProduct, int currentPosition);
        void onCardLongClick(int currentPosition);
    }
}
