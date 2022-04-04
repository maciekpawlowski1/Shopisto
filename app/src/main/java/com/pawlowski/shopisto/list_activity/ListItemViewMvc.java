package com.pawlowski.shopisto.list_activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.group_activity.ProductsInGroupAdapter;
import com.pawlowski.shopisto.models.ProductModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ListItemViewMvc extends BaseObservableViewMvc<ListItemViewMvc.ListItemButtonsClickListener> {

    private final TextView tittleText;
    private final TextView descriptionText;
    private final TextView numberText;
    private final ImageView imageView;
    private final CheckBox checkBox;
    private final CardView cardView;
    private final ConstraintLayout constraintLayout;

    private int currentPosition;
    private ProductModel currentProduct;

    ListItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.product_in_list_card, viewGroup, false);

        tittleText = findViewById(R.id.tittle_list_card);
        descriptionText = findViewById(R.id.description_list_card);
        numberText = findViewById(R.id.number_list_card);
        imageView = findViewById(R.id.image_list_card);
        checkBox = findViewById(R.id.check_box_list_card);
        cardView = findViewById(R.id.card_view_product_in_list_card);
        constraintLayout = findViewById(R.id.constaint_layout_product_in_list_card);


        cardView.setOnClickListener(v -> {
            for(ListItemButtonsClickListener l:listeners)
            {
                l.onCardClick(currentPosition);
            }
        });

        cardView.setOnLongClickListener(v -> {
            for(ListItemButtonsClickListener l:listeners)
            {
                l.onCardLongClick(currentPosition);
            }
            return true;
        });

        constraintLayout.setOnClickListener(v -> {
            for(ListItemButtonsClickListener l:listeners)
            {
                l.onConstraintClick(currentPosition);
            }
        });

        imageView.setOnClickListener(v -> {
            for(ListItemButtonsClickListener l:listeners)
            {
                l.onImageClick(currentProduct);
            }
        });

        checkBox.setOnClickListener(v -> {
            for(ListItemButtonsClickListener l:listeners)
            {
                l.onCheckBoxClick(currentProduct, ListItemViewMvc.this);
            }
        });
    }

    public void changeCheckedOfCheckBox()
    {
        checkBox.setChecked(!checkBox.isChecked());
    }

    public boolean isCheckBoxChecked()
    {
        return checkBox.isChecked();
    }

    public void animateCheckBox()
    {
        ScaleAnimation scaleAnimation;
        BounceInterpolator bounceInterpolator;
        scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);
        checkBox.startAnimation(scaleAnimation);
        cardView.startAnimation(scaleAnimation);
    }

    public void bindProduct(ProductModel currentProduct, int position, boolean isSelected)
    {
        this.currentPosition = position;
        this.currentProduct = currentProduct;

        tittleText.setText(currentProduct.getTittle());
        descriptionText.setText(currentProduct.getDescription());
        numberText.setText(currentProduct.getNumber()+"");
        checkBox.setChecked(currentProduct.isSelected());

        if(currentProduct.isSelected())
        {
            cardView.setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.card_backgroubd_color2));
            imageView.setAlpha(0.6f);
            tittleText.setAlpha(0.7f);
            numberText.setAlpha(0.7f);
        }
        else
        {
            cardView.setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.white));
            imageView.setAlpha(1.0f);
            tittleText.setAlpha(1.0f);
            numberText.setAlpha(1.0f);
        }

        ProductsInGroupAdapter.changeCategoryImageDependingOnCategory(imageView, currentProduct.getCategoryId());

        if(isSelected)
        {
            cardView.setCardBackgroundColor(rootView.getContext().getResources().getColor(R.color.selected_color));
            checkBox.setEnabled(false);
        }
        else
        {
            checkBox.setEnabled(true);
        }
    }

    interface ListItemButtonsClickListener {
        void onCardClick(int currentPosition);
        void onCardLongClick(int currentPosition);
        void onConstraintClick(int currentPosition);
        void onImageClick(ProductModel currentProduct);
        void onCheckBoxClick(ProductModel currentProduct, ListItemViewMvc viewMvc);
    }
}
