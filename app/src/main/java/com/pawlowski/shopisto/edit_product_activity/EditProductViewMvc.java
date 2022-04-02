package com.pawlowski.shopisto.edit_product_activity;

import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.filters.MyFilters;
import com.pawlowski.shopisto.models.ProductModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EditProductViewMvc extends BaseObservableViewMvc<EditProductViewMvc.EditProductButtonsClickListener> {

    private final EditText tittleEditText;
    private final EditText descriptionEditText;
    private final EditText numberEditText;
    private final ImageButton micTittle;
    private final ImageButton micDescription;
    private final RecyclerView categoryRecycler;


    public EditProductViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_edit_product, viewGroup, false);
        tittleEditText = findViewById(R.id.tittle_edit_edit_product);
        descriptionEditText = findViewById(R.id.description_edit_edit_product);
        numberEditText = findViewById(R.id.number_edit_edit_product);
        micTittle = findViewById(R.id.mic_button_edit_product);
        micDescription = findViewById(R.id.mic_button_edit_product2);
        categoryRecycler = findViewById(R.id.category_recycler_edit_product);
        tittleEditText.setFilters(new InputFilter[]{MyFilters.getTittleInputFilter()});
        descriptionEditText.setFilters(new InputFilter[]{MyFilters.getTittleInputFilter()});

        micTittle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(EditProductButtonsClickListener l:listeners)
                {
                    l.onMicTittleClick();
                }
            }
        });

        micDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(EditProductButtonsClickListener l:listeners)
                {
                    l.onMicDescriptionClick();
                }
            }
        });
    }

    public void setRecyclerAdapter(CategoryAdapter adapter)
    {
        categoryRecycler.setAdapter(adapter);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
    }

    public void bindProduct(ProductModel product)
    {
        tittleEditText.setText(product.getTittle());
        if(product.getDescription().equals(" "))
            descriptionEditText.setText("");
        else
            descriptionEditText.setText(product.getDescription());

        numberEditText.setText(product.getNumber()+"");
    }

    public String getTittleInputText()
    {
        Editable editable = tittleEditText.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public String getNumberInputText()
    {
        Editable editable = numberEditText.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public String getDescriptionInputText()
    {
        Editable editable = descriptionEditText.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public void setTittleInput(String tittle)
    {
        tittleEditText.setText(tittle);
    }

    public void setDescriptionInput(String description)
    {
        tittleEditText.setText(description);
    }

    interface EditProductButtonsClickListener
    {
        void onMicTittleClick();
        void onMicDescriptionClick();
    }
}
